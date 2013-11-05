package edu.sjsu.cmpe.library.repository;

import static com.google.common.base.Preconditions.checkArgument;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsDestination;
import edu.sjsu.cmpe.library.config.LibraryServiceConfiguration;
import edu.sjsu.cmpe.library.domain.Book;
import edu.sjsu.cmpe.library.domain.Book.Status;

public class BookRepository implements BookRepositoryInterface {

	/** In-memory map to store books. (Key, Value) -> (ISBN, Book) */
	private final ConcurrentHashMap<Long, Book> bookInMemoryMap;
	LibraryServiceConfiguration config;
	/** Never access this key directly; instead use generateISBNKey() */
	private long isbnKey;
	ArrayList<String> receivedBooks = new ArrayList<String>();

	public BookRepository(LibraryServiceConfiguration configuration)
			throws JMSException, MalformedURLException {
		isbnKey = 0;
		bookInMemoryMap = seedData();
		config = configuration;
		
	}

	private ConcurrentHashMap<Long, Book> seedData() {
		ConcurrentHashMap<Long, Book> bookMap = new ConcurrentHashMap<Long, Book>();
		Book book = new Book();
		book.setIsbn(1);
		book.setCategory("computer");
		book.setTitle("Java Concurrency in Practice");
		try {
			book.setCoverimage(new URL("http://goo.gl/N96GJN"));
		} catch (MalformedURLException e) {
			// eat the exception
		}
		bookMap.put(book.getIsbn(), book);

		book = new Book();
		book.setIsbn(2);
		book.setCategory("computer");
		book.setTitle("Restful Web Services");
		try {
			book.setCoverimage(new URL("http://goo.gl/ZGmzoJ"));
		} catch (MalformedURLException e) {
			// eat the exception
		}
		bookMap.put(book.getIsbn(), book);

		return bookMap;
	}

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
		if(bookInMemoryMap.get(isbn) != null){
		return bookInMemoryMap.get(isbn);
		}
	else{
			Book book=new Book();
			return book;
			}
		
	}

	@Override
	public List<Book> getAllBooks() {
		System.out.println("Returning all books from book Repo" +bookInMemoryMap.values());
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

	public Book update(Long isbn, Status newStatus) throws JMSException {
		Book book = bookInMemoryMap.get(isbn);
		book.setStatus(newStatus);
		System.out.println("in book update book is " + book.getStatus());

		if (config.getStompTopicName().equals("/topic/68935.book.all")) {
			 callProducer("library-a:"+isbn);
			 
			 
		} else {
			 callProducer("library-b:"+isbn);
		}
		return book;
	}

	private static String env(String key, String defaultValue) {
		String rc = System.getenv(key);
		if (rc == null) {
			return defaultValue;
		}
		return rc;
	}

	public void callProducer(String name) throws JMSException {
		System.out.println("in callProducer " + config.getApolloUser());
		String user = env1("APOLLO_USER", config.getApolloUser());
		String password = env1("APOLLO_PASSWORD", config.getApolloPassword());
		String host = env1("APOLLO_HOST", config.getApolloHost());
		int port = config.getApolloPort();
		// String queue = "/queue/mytesting";
		// String destination = arg(args, 0, queue);

		StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
		factory.setBrokerURI("tcp://" + host + ":" + port);

		Connection connection = factory.createConnection(user, password);
		connection.start();
		Session session = connection.createSession(false,
				Session.AUTO_ACKNOWLEDGE);
		Destination dest = new StompJmsDestination(config.getStompQueueName());// destination);
		MessageProducer producer = session.createProducer(dest);
		// producer.setDeliveryMode(DeliveryMode.PERSISTENT);
		// producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

		System.out.println("Sending messages to " + config.getStompQueueName()+ "...");
		// String data = "library-a";
		TextMessage msg = session.createTextMessage(name);
		// String msgqueue=name+",";
		producer.send(msg);

		// msg.setLongProperty("id", System.currentTimeMillis());
		// producer.send(session.createTextMessage("SHUTDOWN"));
		connection.close();

	}

	private static String env1(String key, String defaultValue) {
		String rc = System.getenv(key);
		if (rc == null) {
			return defaultValue;
		}
		return rc;
	}

	public void listener() throws JMSException, MalformedURLException {
		long isbn;
		String bootTitle;
		String bookCategory;
		URL webUrl;
		Book receivedBookItems;
		String user = env("APOLLO_USER", "admin");
		String password = env("APOLLO_PASSWORD", "password");
		String host = env("APOLLO_HOST", "54.215.210.214");
		int port = Integer.parseInt(env("APOLLO_PORT", "61613"));
		StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
		factory.setBrokerURI("tcp://" + host + ":" + port);
		while (true) {

			Connection connection = factory.createConnection(user, password);
			connection.start();
			Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			Destination dest = new StompJmsDestination(
					config.getStompTopicName());

			MessageConsumer consumer = session.createConsumer(dest);
			System.currentTimeMillis();
			// System.out.println("Waiting for messages.....");
			while (true) {
				Message msg = consumer.receive(500);
				if (msg == null)
					break;
				if (msg instanceof TextMessage) {
					String body = ((TextMessage) msg).getText();
					System.out.println("Received message =  " + body);
					receivedBooks.add(body);
					System.out.println("check received books "+receivedBooks);

				} else {
					System.out.println("unexpected msg " + msg.getClass());
				}
			}
			connection.close();
			for (String books : receivedBooks) {
				isbn = Long.parseLong(books.split(":\"")[0]);
				bootTitle = books.split(":\"")[1].replaceAll("^\"|\"$", "");
				bookCategory = books.split(":\"")[2].replaceAll("^\"|\"$", "");
				String str = books.split(":\"")[3];// .replaceAll("^\"|\"$",
													// "");//url
				receivedBookItems = getBookByISBN(isbn);
				if (receivedBookItems.getStatus().equals(Status.lost)) {
					receivedBookItems.setStatus(Status.available);
				} else if (receivedBookItems.getIsbn() != isbn) {
					receivedBookItems.setIsbn(isbn);
					receivedBookItems.setCategory(bookCategory);
					// receivedBookItems.setCoverimage(webUrl);
					receivedBookItems.setStatus(Status.available);
					receivedBookItems.setTitle(bootTitle);
					System.out.println("new book received is  "
							+ receivedBookItems.toString());
					// receivedBooks.add(body);
					// System.out.println("receivedBooks whole array "+receivedBooks);
				}
			}
		}
	}

	@Override
	public void add(Book newbook) {
		bookInMemoryMap.put(newbook.getIsbn(), newbook);
		System.out.println("added new book in add method "+newbook.getTitle());
		System.out.println(" "+bookInMemoryMap.values());
	}
}
// for(String books:receivedBooks)
// {
// isbn=Long.parseLong(books.split(":")[0]);
// bootTitle=books.split(":")[1].replaceAll("^\"|\"$", "");
// bookCategory=books.split(":")[2].replaceAll("^\"|\"$", "");
// webUrl=new URL(books.split(":")[3].replaceAll("^\"|\"$", ""));
// receivedBookItems=getBookByISBN(isbn);
// if(receivedBookItems.getStatus().equals(Status.lost)){
// receivedBookItems.setStatus(Status.available);
// }
// else
// {
// receivedBookItems.setIsbn(isbn);
// receivedBookItems.setCategory(bookCategory);
// receivedBookItems.setCoverimage(webUrl);
// receivedBookItems.setStatus(Status.available);
// receivedBookItems.setTitle(bootTitle);
// System.out.println("new book received is  "+receivedBookItems);
//
// }
// }
// }

