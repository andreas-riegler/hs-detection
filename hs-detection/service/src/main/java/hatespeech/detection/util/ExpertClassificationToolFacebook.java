package hatespeech.detection.util;

import hatespeech.detection.dao.JDBCFBCommentDAO;
import hatespeech.detection.dao.JDBCTwitterDAO;
import hatespeech.detection.model.FBComment;
import hatespeech.detection.model.Tweet;
import hatespeech.detection.model.TweetImage;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.DefaultEditorKit.CutAction;

public class ExpertClassificationToolFacebook extends JFrame{

	private static final long serialVersionUID = 1L;
	private JLabel messageLabel;
	private JPanel controlPanel,imagePanel;
	private List<FBComment> fbCommentList;
	private int currentCommentId;
	private JDBCFBCommentDAO jdbcFBCommentDAO;

	private enum FBCommentType{
		TEXT, IMAGE
	}

	public ExpertClassificationToolFacebook(int count, FBCommentType commentType)
	{
		initGUI();
		jdbcFBCommentDAO = new JDBCFBCommentDAO();

		if(commentType == FBCommentType.TEXT){
			fbCommentList = jdbcFBCommentDAO.getRandomUnclassifiedTextFBCommentsByCount(count);
		}
		else if(commentType == FBCommentType.IMAGE){
			fbCommentList = jdbcFBCommentDAO.getRandomUnclassifiedImageFBCommentsByCount(count);
		}

		currentCommentId = 0;
	}
	
	public ExpertClassificationToolFacebook(int count, FBCommentType commentType, String word)
	{
		initGUI();
		jdbcFBCommentDAO = new JDBCFBCommentDAO();

		if(commentType == FBCommentType.TEXT){
			fbCommentList = jdbcFBCommentDAO.getRandomUnclassifiedTextContainingWordFBCommentsByCount(count, '%' + word + '%');
		}
		else if(commentType == FBCommentType.IMAGE){
			fbCommentList = jdbcFBCommentDAO.getRandomUnclassifiedImageFBCommentsByCount(count);
		}

		currentCommentId = 0;
	}

	private void initGUI() {
		setTitle("Facebook Klassifizierungstool");
		setSize(600, 200);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new GridLayout(3, 1));

		messageLabel = new JLabel("", JLabel.CENTER);

		controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout());
		imagePanel = new JPanel();
		imagePanel.setLayout(new FlowLayout());

		this.add(messageLabel);
		this.add(imagePanel);
		this.add(controlPanel);
		this.setVisible(true);  

	}

	private void initializeClassification() {

		messageLabel.setText("<html><center>" + getLabelText() + "</center></html>"); 

		JButton imgButton=new JButton("IMG");

		imgButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String imageUrl = getImage();

					if(imageUrl != null){
						BufferedImage image = ImageIO.read(new File(imageUrl));

						JLabel lbl = new JLabel(new ImageIcon(image));
						JOptionPane.showMessageDialog(null, lbl, "ImageDialog",	JOptionPane.PLAIN_MESSAGE, null);
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		imagePanel.add(imgButton);

		JButton hateButton = new JButton("Hassrede");
		hateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jdbcFBCommentDAO.updateResult(fbCommentList.get(currentCommentId).getId(), 1);
				nextComment();
				refreshLabelText();
			}
		});
		JButton insultButton = new JButton("Beleidigung");
		insultButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jdbcFBCommentDAO.updateResult(fbCommentList.get(currentCommentId).getId(), 2);
				nextComment();
				refreshLabelText();
			}
		});
		JButton otherOffensiveContentButton = new JButton("sonst. off. Inhalt");
		otherOffensiveContentButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jdbcFBCommentDAO.updateResult(fbCommentList.get(currentCommentId).getId(), 3);
				nextComment();
				refreshLabelText();
			}
		});
		JButton noButton = new JButton("Neutral");
		noButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jdbcFBCommentDAO.updateResult(fbCommentList.get(currentCommentId).getId(), 0);
				nextComment();
				refreshLabelText();
			}
		}); 
		JButton nextButton = new JButton("Weiter");
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nextComment();
				refreshLabelText();
			}
		});

		controlPanel.add(hateButton);
		controlPanel.add(insultButton);
		controlPanel.add(otherOffensiveContentButton);
		controlPanel.add(noButton);
		controlPanel.add(nextButton);       

		this.setVisible(true); 
	}

	private void nextComment(){
		currentCommentId++;
	}

	private void refreshLabelText() {
		System.out.println(getLabelText());
		messageLabel.setText("<html><center>" + getLabelText() + "</center></html>");
	}

	private String getLabelText() {
		if(currentCommentId < fbCommentList.size()){
			return fbCommentList.get(currentCommentId).getMessage();
		}
		else{
			this.remove(controlPanel);
			this.revalidate();
			this.repaint();
			return "Fertig! Sehr Brav!";
		}
	}

	private String getImage() {
		if(currentCommentId < fbCommentList.size()){
			return fbCommentList.get(currentCommentId).getAttachmerntMediaImageSrc();
		}
		else{
			this.remove(controlPanel);
			this.revalidate();
			this.repaint();
			return null;
		}
	}

	public static void main(String[] args) {
		ExpertClassificationToolFacebook exptClass = new ExpertClassificationToolFacebook(30, FBCommentType.TEXT);
		//ExpertClassificationToolFacebook exptClass = new ExpertClassificationToolFacebook(30, FBCommentType.TEXT, "hure");
		exptClass.initializeClassification();
	}

}
