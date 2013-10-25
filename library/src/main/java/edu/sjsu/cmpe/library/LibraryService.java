package edu.sjsu.cmpe.library;
//http://54.215.210.214:61680/
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.views.ViewBundle;

import edu.sjsu.cmpe.library.api.resources.BookResource;
import edu.sjsu.cmpe.library.api.resources.RootResource;
import edu.sjsu.cmpe.library.config.LibraryServiceConfiguration;
import edu.sjsu.cmpe.library.domain.Book;
import edu.sjsu.cmpe.library.repository.BookRepository;
import edu.sjsu.cmpe.library.repository.BookRepositoryInterface;
import edu.sjsu.cmpe.library.ui.resources.HomeResource;

public class LibraryService extends Service<LibraryServiceConfiguration> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) throws Exception {
	new LibraryService().run(args);
    }

    @Override
    public void initialize(Bootstrap<LibraryServiceConfiguration> bootstrap) {
	bootstrap.setName("library-service");
	bootstrap.addBundle(new ViewBundle());
    }

    @Override
    public void run(LibraryServiceConfiguration configuration,
	    Environment environment) throws Exception {
	// This is how you pull the configurations from library_x_config.yml
	String queueName = configuration.getStompQueueName();
	String topicName = configuration.getStompTopicName();
	String apollouser=configuration.getApolloUser();
	String apolloPassword=configuration.getApolloPassword();
	String apollohost=configuration.getApolloHost();
	String apolloPort=configuration.getApolloPort();
	log.debug("Queue name is {}. Topic name is {}. User is {}, password is {}. host is {}. port is {}", queueName,topicName,apollouser,apolloPassword,apollohost,apolloPort);
	// TODO: Apollo STOMP Broker URL and login
	// Do from the library_a_config.yml file
	
	//--------------------------------------------------------------------------

	/** Root API */
	environment.addResource(RootResource.class);
	/** Books APIs */
	BookRepositoryInterface bookRepository = new BookRepository(configuration);
	BookRepository bookrepoactions=new BookRepository(configuration);
	environment.addResource(new BookResource(bookRepository,bookrepoactions));

	/** UI Resources */
	environment.addResource(new HomeResource(bookRepository));
    }
}
