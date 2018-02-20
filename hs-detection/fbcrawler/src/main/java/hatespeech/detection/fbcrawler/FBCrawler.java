package hatespeech.detection.fbcrawler;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.exception.FacebookNetworkException;
import com.restfb.types.Comment;
import com.restfb.types.Comment.Attachment;
import com.restfb.types.Post;
import com.restfb.types.Reactions.ReactionItem;
import com.restfb.util.CachedDateFormatStrategy;
import com.restfb.util.DateFormatStrategy;
import com.restfb.util.DateUtils;

import hatespeech.detection.dao.JDBCFBCommentDAO;
import hatespeech.detection.model.FBComment;
import hatespeech.detection.model.FBPost;
import hatespeech.detection.model.FBReaction;


public class FBCrawler {

	private JDBCFBCommentDAO fbCommentDAO = new JDBCFBCommentDAO();

	private FacebookClient facebookClient;
	private AccessToken accessToken;

	private String appid, appsecret;

	private Calendar cal = Calendar.getInstance();
	private Pattern extensionPattern = Pattern.compile("[^a-zA-Z0-9]");

	public static void main(String[] args) {

		DateUtils.setDateFormatStrategy(new CachedDateFormatStrategy());

		FBCrawler fbc = new FBCrawler();

		fbc.loadProperties("config.properties");

		fbc.init();

		try {

			System.out.println("Application access token: " + fbc.accessToken.getAccessToken());

			//			System.out.println("initial NoParasiten");
			//			fbc.crawlPostsAndCommentsOfPageInitial("NoParasiten");

			//while(true){
			System.out.println("latest pegida.at");
			//fbc.crawlPostsAndCommentsOfPageStream("pegida.at", 100);
			//Thread.sleep(10000);
			System.out.println("latest 549362128466778");
			//fbc.crawlPostsAndCommentsOfPageStream("549362128466778", 50);
			//Thread.sleep(10000);
			//System.out.println("latest 911598655526244");
			//fbc.crawlPostsAndCommentsOfPageStream("911598655526244", 2);
			//Thread.sleep(10000);
			System.out.println("latest pegidaevdresden");
			//fbc.crawlPostsAndCommentsOfPageStream("pegidaevdresden", 50);
			//Thread.sleep(10000);
			System.out.println("latest alternativefuerde");
			fbc.crawlPostsAndCommentsOfPageStream("alternativefuerde", 50);
			Thread.sleep(10000);
			//}

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

	@Deprecated
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

					FBPost fbp = new FBPost(post.getId(), post.getCommentsCount(), post.getCreatedTime(), (post.getFrom() != null ? post.getFrom().getId() : null), post.getLikesCount(), 
							post.getMessage(), post.getSharesCount(), post.getType(), post.getDescription(), post.getCaption(), post.getFullPicture(), post.getIsExpired(), 
							post.getIsHidden(), post.getIsPublished(), post.getLink(), post.getName(), post.getPermalinkUrl(), post.getStory(), post.getTimelineVisibility(), post.getReactionsCount());

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
										comment.getLikeCount(), comment.getMessage(), (comment.getParent() != null ? comment.getParent().getId() : null), comment.getIsHidden(),
										(comment.getAttachment() != null && comment.getAttachment().getMedia() != null && comment.getAttachment().getMedia().getImage() != null ? 
												comment.getAttachment().getMedia().getImage().getSrc() : null));

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
														reply.getLikeCount(), reply.getMessage(), (reply.getParent() != null ? reply.getParent().getId() : null), reply.getIsHidden(),
														(reply.getAttachment() != null && reply.getAttachment().getMedia() != null && reply.getAttachment().getMedia().getImage() != null ? 
																reply.getAttachment().getMedia().getImage().getSrc() : null));

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

	@Deprecated
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

					FBPost fbp = new FBPost(post.getId(), post.getCommentsCount(), post.getCreatedTime(), (post.getFrom() != null ? post.getFrom().getId() : null), post.getLikesCount(), 
							post.getMessage(), post.getSharesCount(), post.getType(), post.getDescription(), post.getCaption(), post.getFullPicture(), post.getIsExpired(), 
							post.getIsHidden(), post.getIsPublished(), post.getLink(), post.getName(), post.getPermalinkUrl(), post.getStory(), post.getTimelineVisibility(), post.getReactionsCount());

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
												comment.getLikeCount(), comment.getMessage(), (comment.getParent() != null ? comment.getParent().getId() : null), comment.getIsHidden(),
												(comment.getAttachment() != null && comment.getAttachment().getMedia() != null && comment.getAttachment().getMedia().getImage() != null ? 
														comment.getAttachment().getMedia().getImage().getSrc() : null));

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

	public void crawlPostsAndCommentsOfPageStream(String page, int maxNumberOfPages){

		List<FBComment> commentListParent = new ArrayList<FBComment>();
		List<FBComment> commentListChild = new ArrayList<FBComment>();

		int pageCount = 0;

		Connection<Post> pageFeed;

		try{
			//all pages of posts
			pageFeed = this.facebookClient.fetchConnection(page + "/feed", Post.class,
					Parameter.with("fields", "created_time,from,id,message,type,comments.limit(1).summary(true).filter(stream){id},likes.limit(1).summary(true){id},reactions.limit(1).summary(true){id},"
							+ "shares,description,caption,full_picture,is_expired,is_hidden,is_published,link,name,permalink_url,status_type,timeline_visibility"),
							Parameter.with("limit", 100),
							Parameter.with("show_expired", "true"),
							Parameter.with("include_hidden", "true"));

			//iterate through all post-pages
			for (List<Post> feedConnectionPage : pageFeed){

				if(pageCount == maxNumberOfPages){
					return;
				}

				System.out.println("Post-Page " + ++pageCount);

				//iterate through all posts of a page
				for (Post post : feedConnectionPage){
					int commentCountPerPost = 0;

					FBPost fbp = new FBPost(post.getId(), post.getCommentsCount(), post.getCreatedTime(), (post.getFrom() != null ? post.getFrom().getId() : null), post.getLikesCount(), 
							post.getMessage(), post.getSharesCount(), post.getType(), post.getDescription(), post.getCaption(), null, post.getIsExpired(), 
							post.getIsHidden(), post.getIsPublished(), post.getLink(), post.getName(), post.getPermalinkUrl(), post.getStatusType(), post.getTimelineVisibility(), post.getReactionsCount());

					if(!fbCommentDAO.existsFBPostId(post.getId())){
						String postFullPicturePath = saveImage(post.getFullPicture(), post.getId(), "p");
						fbp.setFullPicture(postFullPicturePath);

						//insert post into DB
						fbCommentDAO.insertFBPost(fbp);
						System.out.println("Post insert");
					}

					Connection<Comment> postComments;

					try{
						postComments = this.facebookClient.fetchConnection(post.getId().toString() + "/comments", Comment.class,
								Parameter.with("fields", "from,message,comment_count,created_time,id,like_count,parent,is_hidden,attachment"),
								Parameter.with("limit", 1000),
								Parameter.with("order", "reverse_chronological"),
								Parameter.with("filter", "stream"));
					}
					catch(Exception e){
						System.out.println(e.getMessage());
						continue;
					}

					comments:
						for (List<Comment> commentConnectionPage : postComments){
							for (Comment comment : commentConnectionPage){

								String attachmentMediaImageSrc = null;

								if(comment.getAttachment() != null && comment.getAttachment().getMedia() != null && comment.getAttachment().getMedia().getImage() != null){
									attachmentMediaImageSrc = comment.getAttachment().getMedia().getImage().getSrc();
								}

								if(!comment.getMessage().isEmpty() || attachmentMediaImageSrc != null){
									if(!fbCommentDAO.existsFBCommentId(comment.getId())){

										cal.setTime(comment.getCreatedTime());
										//adds one hour (timezone)
										cal.add(Calendar.HOUR_OF_DAY, 1);
										Date createdTimeComment = cal.getTime();

										String commentPicturePath = saveImage(attachmentMediaImageSrc, comment.getId(), "c");

										FBComment fbc = new FBComment(comment.getId(), post.getId(), createdTimeComment, comment.getCommentCount(), (comment.getFrom() != null ? comment.getFrom().getId() : null),
												comment.getLikeCount(), comment.getMessage(), (comment.getParent() != null ? comment.getParent().getId() : null), comment.getIsHidden(),
												commentPicturePath);

										if(comment.getParent() == null){
											commentListParent.add(fbc);
										}
										else{
											commentListChild.add(fbc);
										}

										commentCountPerPost++;
									}
									else{
										break comments;
									}
								}
							}
						}

					for(FBComment c : commentListParent){
						fbCommentDAO.insertFBComment(c);
					}

					for(FBComment c : commentListChild){
						fbCommentDAO.insertFBComment(c);
					}

					commentListParent.clear();
					commentListChild.clear();

					System.out.println("Comments added for Post " + post.getId() + ": " + commentCountPerPost);


					Connection<ReactionItem> postReactions;
					int reactionCountPerPost = 0;

					try{
						postReactions = this.facebookClient.fetchConnection(post.getId().toString() + "/reactions", ReactionItem.class,
								Parameter.with("fields", "user,type"),
								Parameter.with("limit", 1000));
					}
					catch(Exception e){
						System.out.println(e.getMessage());
						continue;
					}

					for (List<ReactionItem> reactionConnectionPage : postReactions){
						for (ReactionItem reaction : reactionConnectionPage){
							if(!fbCommentDAO.existsFBReaction(post.getId(), reaction.getId())){

								FBReaction fbr = new FBReaction(post.getId(), reaction.getId(), reaction.getType());

								fbCommentDAO.insertFBReaction(fbr);

								reactionCountPerPost++;
							}
						}
					}

					System.out.println("Reactions added for Post " + post.getId() + ": " + reactionCountPerPost);
				}
			}

		}
		catch(Exception e){
			System.out.println(e.getMessage());
			return;
		}
	}

	public String saveImage(String url, String id, String prefix){
		try {
			if(url != null){
				URL imageLocation = new URL(url);

				String filePath = "";

				if(imageLocation.toString().toLowerCase().contains(".jpg")){
					//filePath = "C:/images/" + prefix + id + ".jpg";
					filePath = "/home/andreas/repos/hs-detection/hs-detection/images/images/" + prefix + id + ".jpg";
				}
				else if(imageLocation.toString().toLowerCase().contains(".png")){
					//filePath = "C:/images/" + prefix + id + ".png";
					filePath = "/home/andreas/repos/hs-detection/hs-detection/images/images/" + prefix + id + ".png";
				}
				else if(imageLocation.toString().toLowerCase().contains(".jpeg")){
					//filePath = "C:/images/" + prefix + id + ".jpeg";
					filePath = "/home/andreas/repos/hs-detection/hs-detection/images/images/" + prefix + id + ".jpeg";
				}
				else{
					String extension = imageLocation.toString().substring(imageLocation.toString().lastIndexOf("."));
					Matcher extensionMatcher = extensionPattern.matcher(extension.substring(1));	
					if(extensionMatcher.find()){
						filePath = "/home/andreas/repos/hs-detection/hs-detection/images/images/" + prefix + id + extension.substring(0, extensionMatcher.start() + 1);
					}
					else{
						filePath = "/home/andreas/repos/hs-detection/hs-detection/images/images/" + prefix + id + extension;
					}
				}

				ReadableByteChannel rbc = Channels.newChannel(imageLocation.openStream());
				FileOutputStream outputStream;
				outputStream = new FileOutputStream(filePath);
				outputStream.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				outputStream.close();
				return filePath;
			}
			else{ 
				return null;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.out.println("URL: " + url);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.out.println("URL: " + url);
		}

		return null;
	}
}
