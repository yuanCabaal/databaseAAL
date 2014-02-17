package agents.beans;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import objects.LinkedInUser;
import ontology.messages.LinkedInData;

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;

import twitter4j.IDs;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.google.code.linkedinapi.client.LinkedInApiClient;
import com.google.code.linkedinapi.client.LinkedInApiClientFactory;
import com.google.code.linkedinapi.schema.Person;

import access.MySQLAccess;
import access.UserAccess;
import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.comm.ICommunicationBean;
import de.dailab.jiactng.agentcore.comm.IMessageBoxAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class TwitterBean extends AbstractAgentBean{
	
	private final String consumerKeyValue = "6edjES4nEmLuDMCoRtvkw";
	private final String consumerSecretValue = "V2N8REdkCrDS0mr2msI3HJl4eRAgATM0K6BBUIdt33Y";
	private String accessTokenValue;
	private String tokenSecretValue;
	
	private IActionDescription sendAction = null;
	
	private MySQLAccess access = null;
	private UserAccess userAccess = null;
	private Connection connect = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
	
	
	
	@Override
	public void doStart() throws Exception{
		super.doStart();
		log.info("TwitterAgent started.");
		
		access = new MySQLAccess();
		connect = access.connectDriver();
		userAccess = new UserAccess(connect);
		
		
		if(sendAction == null){
			sendAction = thisAgent.searchAction(template);
		}
		
		if(sendAction == null){
			throw new RuntimeException("Send action not found.");
		}
		
		memory.attach(new MessageObserver(), new JiacMessage());
	}
	
	private class MessageObserver implements SpaceObserver<IFact>{
		
		

		/**
		 * 
		 */
		private static final long serialVersionUID = -8182513339144469591L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {
			if(event instanceof WriteCallEvent<?>){
				WriteCallEvent<IJiacMessage> wce = (WriteCallEvent<IJiacMessage>) event;
				
				IJiacMessage message = memory.remove(wce.getObject());
				
				if(message != null){
					IFact obj = message.getPayload();
					
					if(obj instanceof TwitterData){
						
						try {
							
							  ConfigurationBuilder cb = new ConfigurationBuilder();
							    cb.setDebugEnabled(true)
							      .setOAuthConsumerKey(consumerKeyValue)
							      .setOAuthConsumerSecret(consumerSecretValue)
							      .setOAuthAccessToken(obj.getAccessToken())
							      .setOAuthAccessTokenSecret(obj.getTokenSecret());
							    TwitterFactory tf = new TwitterFactory(cb.build());
							    Twitter twitter = tf.getInstance();
							
							 List<Status> statuses = twitter.getHomeTimeline();
					    	    IDs friendsList = twitter.getFriendsIDs(20);
							
							
							log.info("Name:" + profile.getFirstName() + " " + profile.getLastName());
							log.info("Headline:" + profile.getHeadline());
							log.info("Summary:" + profile.getSummary());
							log.info("Industry:" + profile.getIndustry());
							log.info("Picture:" + profile.getPictureUrl());
							
							LinkedInUser user = new LinkedInUser();
							
							List<IAgentDescription> agentDescriptions = thisAgent.searchAllAgents(new AgentDescription());

							for(IAgentDescription agent : agentDescriptions){
								if(agent.getName().equals("LinkedInAgent")){

									IMessageBoxAddress receiver = agent.getMessageBoxAddress();
									
									LinkedInData data = new LinkedInData(thisAgent.getAgentId(), agent.getAid());
									data.setMe(user);
									JiacMessage newMessage = new JiacMessage(data);

									invoke(sendAction, new Serializable[] {newMessage, receiver});
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					else{
						memory.write(wce.getObject());
					}
				}
				
			}
			
		}
		
	}

}