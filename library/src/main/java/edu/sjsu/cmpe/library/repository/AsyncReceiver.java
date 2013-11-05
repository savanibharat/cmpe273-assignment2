package edu.sjsu.cmpe.library.repository;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.MessageListener;
import javax.jms.JMSException;

import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsDestination;

import edu.sjsu.cmpe.library.config.LibraryServiceConfiguration;
import edu.sjsu.cmpe.library.domain.Book;
import edu.sjsu.cmpe.library.domain.Book.Status;

public class AsyncReceiver implements MessageListener {
	LibraryServiceConfiguration config;
	BookRepositoryInterface bookRepository;
	Book newbook=new Book();
	public AsyncReceiver(LibraryServiceConfiguration config, BookRepositoryInterface bookRepository2)
			throws JMSException {
		this.config = config;
		this.bookRepository = bookRepository2;
		// listernerMsg();
	}

	/**
	 * This method is called asynchronously by JMS when a message arrives at the
	 * queue. Client applications must not throw any exceptions in the onMessage
	 * method.
	 * 
	 * @param message
	 *            A JMS message.
	 */
	public void onMessage(Message message) {
		TextMessage msg = (TextMessage) message;
		try {
			System.out.println("received:::: " + msg.getText());
		} catch (JMSException ex) {
			ex.printStackTrace();
		}
	}

	public void listernerMsg() throws JMSException, MalformedURLException {
		System.out.println("inside method listernerMsg");
		ArrayList<String> receivedBooks = new ArrayList<String>();

		long isbn;
		String bookTitle;
		String bookCategory;
		URL webUrl;
		Book receivedBookItems;
		String user = "admin";
		String password = "password";
		String host = "54.215.210.214";
		int port = Integer.parseInt("61613");

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
			 //System.out.println("Waiting for messages.....");
			while (true) {
				Message msg = consumer.receive(500);
				if (msg == null)
					break;
				if (msg instanceof TextMessage) {
					String body = ((TextMessage) msg).getText();
					System.out.println("Received message =  " + body);
					receivedBooks.add(body);
				} else {
					System.out.println("unexpected msg " + msg.getClass());
				}
			}
			connection.close();
			if(!receivedBooks.isEmpty()){
			for (String books : receivedBooks) 
			{
				System.out.println("reachable");
				isbn = Long.parseLong(books.split(":\"")[0]);
				bookTitle = books.split(":\"")[1].replaceAll("^\"|\"$", "");
				bookCategory = books.split(":\"")[2].replaceAll("^\"|\"$", "");
				String str = books.split(":\"")[3];// .replaceAll("^\"|\"$",// "");//url
				str = str.substring(0,str.length()-1);
				webUrl=new URL(str);
				System.out.println(webUrl);
				receivedBookItems = bookRepository.getBookByISBN(isbn);
				
				if(receivedBookItems.getIsbn()==0){
					System.out.println("addding new book");
					receivedBookItems.setIsbn(isbn);
					receivedBookItems.setCategory(bookCategory);
					receivedBookItems.setCoverimage(webUrl);
					receivedBookItems.setStatus(Status.available);
					receivedBookItems.setTitle(bookTitle);
					//receivedBooks.add(receivedBookItems);
					bookRepository.add(receivedBookItems);
					System.out.println("new book received is  "+ receivedBookItems.toString());
				}
				else{//if (receivedBookItems.getStatus().equals(Status.lost)) {
					receivedBookItems.setStatus(Status.available);
				}
				
//				else if (receivedBookItems.getIsbn() != isbn) {
//					receivedBookItems.setIsbn(isbn);
//					receivedBookItems.setCategory(bookCategory);
//					// receivedBookItems.setCoverimage(webUrl);
//					receivedBookItems.setStatus(Status.available);
//					receivedBookItems.setTitle(bookTitle);
//					System.out.println("new book received is  "
//							+ receivedBookItems.toString());
//					// receivedBooks.add(body);
//					// System.out.println("receivedBooks whole array "+receivedBooks);
//				}
			}
			receivedBooks.clear();
		}

	}
	}
}