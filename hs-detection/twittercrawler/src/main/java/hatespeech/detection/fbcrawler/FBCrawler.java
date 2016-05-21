package hatespeech.detection.fbcrawler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.exception.FacebookNetworkException;
import com.restfb.types.Comment;
import com.restfb.types.Post;
import com.restfb.util.CachedDateFormatStrategy;
import com.restfb.util.DateFormatStrategy;
import com.restfb.util.DateUtils;

import hatespeech.detection.dao.JDBCFBCommentDAO;
import hatespeech.detection.model.FBComment;
import hatespeech.detection.model.FBPost;


public class FBCrawler {

	private JDBCFBCommentDAO fbCommentDAO = new JDBCFBCommentDAO();

	private FacebookClient facebookClient;
	private AccessToken accessToken;

	private String appid, appsecret;

	private Calendar cal = Calendar.getInstance();

	public static void main(String[] args) {

		DateUtils.setDateFormatStrategy(new CachedDateFormatStrategy());

		FBCrawler fbc = new FBCrawler();

		fbc.loadProperties("config.properties");

		fbc.init();

		try {

			System.out.println("Application access token: " + fbc.accessToken.getAccessToken());

			//			System.out.println("initial NoParasiten");
			//			fbc.crawlPostsAndCommentsOfPageInitial("NoParasiten");
			//			Thread.sleep(60000);
			//			System.out.println("initial 911598655526244");
			//			fbc.crawlPostsAndCommentsOfPageInitial("911598655526244");
			//			Thread.sleep(60000);
			//			System.out.println("initial pegida.at");
			//			fbc.crawlPostsAndCommentsOfPageInitial("pegida.at");
			//			Thread.sleep(60000);
			//			System.out.println("initial pegidaevdresden");
			//			fbc.crawlPostsAndCommentsOfPageInitial("pegidaevdresden");
			//			Thread.sleep(60000);

			while(true){
				//				System.out.println("latest NoParasiten");
				//				fbc.crawlPostsAndCommentsOfPageLatest("NoParasiten", 5);
				//				Thread.sleep(30000);
				System.out.println("latest 549362128466778");
				fbc.crawlPostsAndCommentsOfPageLatest("549362128466778", 3);
				Thread.sleep(10000);
				System.out.println("latest 911598655526244");
				fbc.crawlPostsAndCommentsOfPageLatest("911598655526244", 3);
				Thread.sleep(10000);
				System.out.println("latest pegida.at");
				fbc.crawlPostsAndCommentsOfPageLatest("pegida.at", 3);
				Thread.sleep(10000);
				System.out.println("latest pegidaevdresden");
				fbc.crawlPostsAndCommentsOfPageLatest("pegidaevdresden", 3);
				Thread.sleep(100000);
			}

		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
		}

		DateFormatStrategy strategy = DateUtils.getDateFormatStrategy();
		if (strategy instanceof CachedDateFormatStrategy)
			((CachedDateFormatStrategy)strategy).clearThreadLocal();
	}

	public void init(){
		this.accessToken = new DefaultFacebookClient(Version.LATEST).obtainAppAccessToken(this.appid, this.appsecret);
		this.facebookClient = new DefaultFacebookClient(this.accessToken.getAccessToken(), Version.LATEST);
	}


	public void loadProperties(String propertiesFile){
		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = getClass().getClassLoader().getResourceAsStream(propertiesFile);


			if (input != null) {
				// load a properties file
				prop.load(input);
			} else {
				throw new FileNotFoundException("property file '" + propertiesFile + "' not found in the classpath");
			}

			// get the property value
			this.appid = prop.getProperty("appid");
			this.appsecret = prop.getProperty("appsecret");

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void crawlPostsAndCommentsOfPageInitial(String page){

		int pageCount = 0;

		Connection<Post> pageFeed;

		try{
			//all pages of posts
			pageFeed = this.facebookClient.fetchConnection(page + "/feed", Post.class, Parameter.with("fields", "created_time,from,id,message,type,comments.limit(1).summary(true){id},likes.limit(1).summary(true){id},shares"), Parameter.with("limit", 100));

			//iterate through all post-pages
			for (List<Post> feedConnectionPage : pageFeed){

				System.out.println("Post-Page " + ++pageCount);

				//iterate through all posts of a page
				for (Post post : feedConnectionPage){

					int commentCountPerPost = 0;

					FBPost fbp = new FBPost(post.getId(), post.getCommentsCount(), post.getCreatedTime(), (post.getFrom() != null ? post.getFrom().getId() : null), post.getLikesCount(), post.getMessage(), post.getSharesCount(), post.getType());

					//insert post into DB
					fbCommentDAO.insertFBPost(fbp);

					Connection<Comment> postComments;
					try{
						postComments = this.facebookClient.fetchConnection(post.getId().toString() + "/comments", Comment.class, Parameter.with("fields", "from,message,comment_count,created_time,id,like_count,parent"), Parameter.with("limit", 100));
					}
					catch(Exception e){
						System.out.println(e.getMessage());
						continue;
					}


					for (List<Comment> commentConnectionPage : postComments){
						for (Comment comment : commentConnectionPage){
							if(!comment.getMessage().isEmpty() || comment.getCommentCount() > 0){

								cal.setTime(comment.getCreatedTime());
								//adds one hour (timezone)
								cal.add(Calendar.HOUR_OF_DAY, 1);
								Date createdTimeComment = cal.getTime();

								FBComment fbc = new FBComment(comment.getId(), post.getId(), createdTimeComment, comment.getCommentCount(), (comment.getFrom() != null ? comment.getFrom().getId() : null),
										comment.getLikeCount(), comment.getMessage(), (comment.getParent() != null ? comment.getParent().getId() : null));

								//insert comment into DB
								fbCommentDAO.insertFBComment(fbc);
								commentCountPerPost++;

								//does the comment have replies?
								if(fbc.getCommentCount() > 0){

									Connection<Comment> replyComments;
									try{
										replyComments = this.facebookClient.fetchConnection(fbc.getId().toString() + "/comments", Comment.class, Parameter.with("fields", "from,message,comment_count,created_time,id,like_count,parent"), Parameter.with("limit", 100));
									}
									catch(Exception e){
										System.out.println(e.getMessage());
										continue;
									}

									for (List<Comment> commentRepliesConnectionPage : replyComments){
										for (Comment reply : commentRepliesConnectionPage){
											if(!reply.getMessage().isEmpty()){

												cal.setTime(reply.getCreatedTime());
												//adds one hour (timezone)
												cal.add(Calendar.HOUR_OF_DAY, 1);
												Date createdTimeReply = cal.getTime();

												FBComment fbcr = new FBComment(reply.getId(), post.getId(), createdTimeReply, reply.getCommentCount(), (reply.getFrom() != null ? reply.getFrom().getId() : null),
														reply.getLikeCount(), reply.getMessage(), (reply.getParent() != null ? reply.getParent().getId() : null));

												//insert reply into DB
												fbCommentDAO.insertFBComment(fbcr);
												commentCountPerPost++;
											}
										}
									}
								}
							}
						}
					}

					System.out.println("Comments added for Post " + post.getId() + ": " + commentCountPerPost);
				}
			}

		}
		catch(Exception e){
			System.out.println(e.getMessage());
			return;
		}
	}

	public void crawlPostsAndCommentsOfPageLatest(String page, int maxNumberOfPages){

		Stack<FBComment> commentStack = new Stack<FBComment>();

		int pageCount = 0;

		Connection<Post> pageFeed;

		try{
			//all pages of posts
			pageFeed = this.facebookClient.fetchConnection(page + "/feed", Post.class, Parameter.with("fields", "created_time,from,id,message,type,comments.limit(1).summary(true){id},likes.limit(1).summary(true){id},shares"), Parameter.with("limit", 100));

			//iterate through all post-pages
			for (List<Post> feedConnectionPage : pageFeed){

				if(pageCount == maxNumberOfPages){
					return;
				}

				System.out.println("Post-Page " + ++pageCount);

				//iterate through all posts of a page
				for (Post post : feedConnectionPage){

					int commentCountPerPost = 0;

					FBPost fbp = new FBPost(post.getId(), post.getCommentsCount(), post.getCreatedTime(), (post.getFrom() != null ? post.getFrom().getId() : null), post.getLikesCount(), post.getMessage(), post.getSharesCount(), post.getType());

					if(!fbCommentDAO.existsFBPostId(post.getId())){
						//insert post into DB
						fbCommentDAO.insertFBPost(fbp);
						System.out.println("Post insert");
					}

					Connection<Comment> postComments;

					try{
						postComments = this.facebookClient.fetchConnection(post.getId().toString() + "/comments", Comment.class, Parameter.with("fields", "from,message,comment_count,created_time,id,like_count,parent"), Parameter.with("limit", 100), Parameter.with("order", "reverse_chronological"), Parameter.with("filter", "stream"));
					}
					catch(Exception e){
						System.out.println(e.getMessage());
						continue;
					}

					comments:
						for (List<Comment> commentConnectionPage : postComments){
							for (Comment comment : commentConnectionPage){
								if(!comment.getMessage().isEmpty() || comment.getCommentCount() > 0){

									if(!fbCommentDAO.existsFBCommentId(comment.getId())){

										cal.setTime(comment.getCreatedTime());
										//adds one hour (timezone)
										cal.add(Calendar.HOUR_OF_DAY, 1);
										Date createdTimeComment = cal.getTime();

										FBComment fbc = new FBComment(comment.getId(), post.getId(), createdTimeComment, comment.getCommentCount(), (comment.getFrom() != null ? comment.getFrom().getId() : null),
												comment.getLikeCount(), comment.getMessage(), (comment.getParent() != null ? comment.getParent().getId() : null));

										//insert comment into commentStack
										commentStack.push(fbc);

										commentCountPerPost++;
									}
									else{
										break comments;
									}
								}
							}
						}

					while(!commentStack.isEmpty()){
						fbCommentDAO.insertFBComment(commentStack.pop());
					}

					System.out.println("Comments added for Post " + post.getId() + ": " + commentCountPerPost);
				}
			}

		}
		catch(Exception e){
			System.out.println(e.getMessage());
			return;
		}
	}
}
