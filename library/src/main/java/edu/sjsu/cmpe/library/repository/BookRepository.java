package edu.sjsu.cmpe.library.repository;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsDestination;
import org.fusesource.stomp.jms.message.StompJmsMessage;

import edu.sjsu.cmpe.library.config.LibraryServiceConfiguration;
import edu.sjsu.cmpe.library.domain.Book;
import edu.sjsu.cmpe.library.domain.Book.Status;

public class BookRepository implements BookRepositoryInterface {
	
	/** In-memory map to store books. (Key, Value) -> (ISBN, Book) */
	private final ConcurrentHashMap<Long, Book> bookInMemoryMap;
	LibraryServiceConfiguration config;
	/** Never access this key directly; instead use generateISBNKey() */
	private long isbnKey;

	public BookRepository(LibraryServiceConfiguration configuration) {
		isbnKey = 0;
		bookInMemoryMap=new ConcurrentHashMap<Long, Book>();
		config=configuration;
	}

	/*
	 * private ConcurrentHashMap<Long, Book> seedData(){ ConcurrentHashMap<Long,
	 * Book> bookMap = new ConcurrentHashMap<Long, Book>(); Book book = new
	 * Book(); book.setIsbn(1); book.setCategory("computer");
	 * book.setTitle("Java Concurrency in Practice"); try {
	 * book.setCoverimage(new URL("http://goo.gl/N96GJN")); } catch
	 * (MalformedURLException e) { // eat the exception }
	 * bookMap.put(book.getIsbn(), book);
	 * 
	 * book = new Book(); book.setIsbn(2); book.setCategory("computer");
	 * book.setTitle("Restful Web Services"); try { book.setCoverimage(new
	 * URL("http://goo.gl/ZGmzoJ")); } catch (MalformedURLException e) { // eat
	 * the exception } bookMap.put(book.getIsbn(), book);
	 * 
	 * return bookMap; }
	 */

	/**
	 * This should be called if and only if you are adding new books to the
	 * repository.
	 * 
	 * @return a new incremental ISBN number
	 */
	private final Long generateISBNKey() {
		// increment existing isbnKey and return the new value
		return Long.valueOf(++isbnKey);
	}

	/**
	 * This will auto-generate unique ISBN for new books.
	 */
	@Override
	public Book saveBook(Book newBook) {
		checkNotNull(newBook, "newBook instance must not be null");
		// Generate new ISBN
		Long isbn = generateISBNKey();
		newBook.setIsbn(isbn);
		// TODO: create and associate other fields such as author

		// Finally, save the new book into the map
		bookInMemoryMap.putIfAbsent(isbn, newBook);

		return newBook;
	}

	/**
	 * @see edu.sjsu.cmpe.library.repository.BookRepositoryInterface#getBookByISBN(java.lang.Long)
	 */
	@Override
	public Book getBookByISBN(Long isbn) {
		checkArgument(isbn > 0,
				"ISBN was %s but expected greater than zero value", isbn);
		return bookInMemoryMap.get(isbn);
	}

	@Override
	public List<Book> getAllBooks() {
		return new ArrayList<Book>(bookInMemoryMap.values());
	}

	/*
	 * Delete a book from the map by the isbn. If the given ISBN was invalid, do
	 * nothing.
	 * 
	 * @see
	 * edu.sjsu.cmpe.library.repository.BookRepositoryInterface#delete(java.
	 * lang.Long)
	 */
	@Override
	public void delete(Long isbn) {
		bookInMemoryMap.remove(isbn);
	}
	public Book update(Long isbn, Status newStatus) throws JMSException
	{
		Book book = bookInMemoryMap.get(isbn);
		book.setStatus(newStatus);
		System.out.println("in book update book is "+book.getStatus());
		
			if(config.getStompTopicName().equals("/topic/68935.book.*"))
				callProducer("library-a:"+isbn);
			else
				callProducer("library-b:"+isbn);
					
		
		return book;
	}
	public void callConsumer() throws JMSException	{
		String user = env("APOLLO_USER", config.getApolloUser());
		String password = env("APOLLO_PASSWORD", config.getApolloPassword());
		String host = env("APOLLO_HOST", config.getApolloHost());
		int port = Integer.parseInt(env("APOLLO_PORT",config.getApolloPort()));
		//String queue = "/queue/mytesting";
		//String destination = arg(args, 0, queue);
		StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
		factory.setBrokerURI("tcp://" + config.getApolloHost() + ":" + port);
		Connection connection = factory.createConnection(user, password);
		connection.start();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination dest = new StompJmsDestination(config.getStompQueueName());//destination);
		MessageConsumer consumer = session.createConsumer(dest);
		System.out.println("Waiting for messages from " + config.getStompQueueName() + "...");
		while(true) {
		    Message msg = consumer.receive();
		    if( msg instanceof  TextMessage ) {
			String body = ((TextMessage) msg).getText();
			System.out.println("Received message = " + body);
			if( "SHUTDOWN".equals(body)) {
			    break;
			}
		    } else if (msg instanceof StompJmsMessage) {
			StompJmsMessage smsg = ((StompJmsMessage) msg);
			String body = smsg.getFrame().contentAsString();
			if ("SHUTDOWN".equals(body)) {
			    break;
			}
			System.out.println("Received message = " + body);
		    } else {
			System.out.println("Unexpected message type: "+msg.getClass());
		    }
		}
		connection.close();	    
	}
	    private static String env(String key, String defaultValue) {
		String rc = System.getenv(key);
		if( rc== null ) {
		    return defaultValue;
		}
		return rc;
	    }

	public void callProducer(String name) throws JMSException {
		System.out.println("in callProducer "+config.getApolloUser());
		String user = env1("APOLLO_USER", config.getApolloUser());
		String password = env1("APOLLO_PASSWORD", config.getApolloPassword());
		String host = env1("APOLLO_HOST", config.getApolloHost());
		int port = Integer.parseInt(env1("APOLLO_PORT",config.getApolloPort() ));
		//String queue = "/queue/mytesting";
		//String destination = arg(args, 0, queue);

		StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
		factory.setBrokerURI("tcp://" + host + ":" + port);

		Connection connection = factory.createConnection(user, password);
		connection.start();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination dest = new StompJmsDestination(config.getStompQueueName());//destination);
		MessageProducer producer = session.createProducer(dest);
		producer.setDeliveryMode(DeliveryMode.PERSISTENT);
		//producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

		System.out.println("Sending messages to " + config.getStompQueueName() + "...");
		//String data = "library-a";
		TextMessage msg = session.createTextMessage(name);
		producer.send(msg);

		msg.setLongProperty("id", System.currentTimeMillis());
		producer.send(session.createTextMessage("SHUTDOWN"));
		connection.close();

	    }

	    private static String env1(String key, String defaultValue) {
		String rc = System.getenv(key);
		if( rc== null ) {
		    return defaultValue;
		}
		return rc;
	    }
}