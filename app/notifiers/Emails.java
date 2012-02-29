package notifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import models.Comment;
import models.Project;
import models.User;
import models.UserStatus;
import play.mvc.Mailer;

public class Emails extends Mailer {
	
	private static final String FROM = "Ceylon repo <ceylon-repo@inforealm.org>";

	public static void confirm(User user) {
		setSubject("[Ceylon repo] Please confirm your email address");
		addRecipient(user.email);
		setFrom(FROM);
		send(user);
	}

	public static void projectClaimNotification(Project project, User user) {
		setSubject("[Ceylon repo] New project claim for "+project.moduleName+" from "+user.userName);
		setFrom(FROM);
		for(String recipient : getNotificationRecipients(null, user)){
			setRecipient(recipient);
			send(project, user);
		}
	}

	public static void projectStatusNotification(Project project, User user) {
		String status = project.status.toString().toLowerCase();
		setSubject("[Ceylon repo] Your project claim "+project.moduleName+" has been "+status);
		setFrom(FROM);
		for(String recipient : getNotificationRecipients(project, user)){
			setRecipient(recipient);
			send(project, status);
		}
	}

	public static void commentNotification(Comment comment, User user) {
		Project project = comment.project;
		setSubject("[Ceylon repo] New comment from "+user.userName+" on project claim "+project.moduleName);
		setFrom(FROM);
		for(String recipient : getNotificationRecipients(project, user)){
			setRecipient(recipient);
			send(project, comment, user);
		}
	}

	private static String[] getNotificationRecipients(Project project, User user) {
		// every admin except current user
		Set<User> users = new HashSet<User>(); 
		users.addAll(User.find("isAdmin = ? AND id != ? AND status = ?", true, user.id, UserStatus.REGISTERED).<User>fetch());
		// possibly the project claimer if not current user
		if(project != null && project.owner != user)
			users.add(project.owner);
		String[] emails = new String[users.size()];
		Iterator<User> iterator = users.iterator();
		for(int i=0;i<emails.length;i++)
			emails[i] = iterator.next().email;
		return emails;
	}
	
	private static void setRecipient(String recipient){
        HashMap<String, Object> map = infos.get();
        List<String> list = new ArrayList<String>();
        list.add(recipient);
        map.put("recipients", list);
	}
}
