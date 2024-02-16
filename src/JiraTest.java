import org.testng.Assert;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import io.restassured.path.json.JsonPath;

import static io.restassured.RestAssured.*;

import java.io.File;
import java.util.List;

public class JiraTest {
	
	//add relaxedHTTPSValidation after given bypass HTTPS security
	
	String SessionName,SessionToken,issueId="10015",commentId="10007";
	String Comment = "This comment will be asserted";
	//Using Session Filter
	SessionFilter  session = new SessionFilter();
	@Test(priority=0)
	public void GetSessionTokenS() {
		RestAssured.baseURI = "http://localhost:8080";
		
		String sessionTokenResponse = given().log().all().header("Content-Type","application/json").
		body("{\r\n"
				+ "    \"username\":\"amanraj\",\r\n"
				+ "    \"password\":\"root\"\r\n"
				+ "}").filter(session).
		when().post("rest/auth/1/session").
		then().log().all().assertThat().statusCode(200).extract().response().asString();
		
		JsonPath ses = new JsonPath(sessionTokenResponse);
		SessionName = ses.getString("session.name");
		SessionToken = ses.getString("session.value");
		System.out.println(SessionName+"="+SessionToken);
	}
	
	@Test(priority=1,enabled=false)
	public void CreateIssueS() {
		
		String createIssueResponse = given().log().all().header("Content-Type","application/json").
		body("{\r\n"
				+ "    \"fields\":{\r\n"
				+ "        \"project\":{\r\n"
				+ "            \"key\":\"RES\"\r\n"
				+ "        },\r\n"
				+ "        \"summary\":\"Credit Card card Defect Coming RestAssured From Session Filter\",\r\n"
				+ "        \"description\":\"This Credit Card Defect is coming from RestAssured  From Session Filter\",\r\n"
				+ "        \"issuetype\":{\r\n"
				+ "            \"name\":\"Bug\"\r\n"
				+ "        }\r\n"
				+ "    }\r\n"
				+ "}").filter(session).
		when().post("rest/api/2/issue").
		then().log().all().assertThat().statusCode(201).extract().response().asString();
		JsonPath issue = new JsonPath(createIssueResponse);
		issueId = issue.getString("id");
		System.out.println("Issue Id: "+issueId);
		
	}
	
	@Test(priority=2,enabled=false)
	public void AddCommentS() {
		String addCommentResponse = given().log().all().pathParam("issueId",issueId ).header("Content-Type","application/json").
		body("{\r\n"
				+ "    \"body\":\""+Comment+"\",\r\n"
				+ "    \"visibility\":{\r\n"
				+ "        \"type\":\"role\",\r\n"
				+ "        \"value\":\"Administrators\"\r\n"
				+ "    }\r\n"
				+ "}").filter(session).
		when().post("rest/api/2/issue/{issueId}/comment").
		then().log().all().assertThat().statusCode(201).extract().response().asString();
		JsonPath comment = new JsonPath(addCommentResponse);
		commentId = comment.getString("id");
		System.out.println("Comment ID: "+commentId);
	}
	
	@Test(priority=3,enabled=false)
	public void AddAttachments() {
		given().log().all().pathParam("issueId", issueId).header("X-Atlassian-Token","no-check").filter(session).
		header("Content-Type","multipart/form-data").
		multiPart("file",new File("Jira.txt")).
		when().post("rest/api/2/issue/{issueId}/attachments").
		then().log().all().assertThat().statusCode(200);
	}
	
	@Test(priority=4)
	public void GetIssueS() {
		String getIssueResponse = given().log().all().pathParam("issueId", issueId).queryParam("fields", "comment").filter(session).
		when().get("rest/api/2/issue/{issueId}").then().log().all().assertThat().statusCode(200).extract().response().asString();
		System.out.println(getIssueResponse);
		
		JsonPath getIssue = new JsonPath(getIssueResponse);
		int commentArraySize = getIssue.getInt("fields.comment.comments.size()");
		System.out.println("Comment Array Size :"+commentArraySize);
		for(int i=0;i<commentArraySize;i++) {
			String ActualCommentID = getIssue.get("fields.comment.comments["+i+"].id");
//			System.out.println("Actual CommentID "+ActualCommentID);
//			System.out.println("Expected CommentID "+commentId);
			if(ActualCommentID.equals(commentId)) {
				String ActualComment = getIssue.get("fields.comment.comments["+i+"].body");
//				System.out.println("Actual Comment "+ActualComment);
//				System.out.println("Expected Comment "+Comment);
				Assert.assertEquals(ActualComment, Comment);
			}
			break;
			
		}
		
	}
	
	// Using Variable
	
	@Test(priority=0,enabled=false)
	public void GetSessionToken() {
		RestAssured.baseURI = "http://localhost:8080";
		
		String sessionTokenResponse = given().log().all().header("Content-Type","application/json").
		body("{\r\n"
				+ "    \"username\":\"amanraj\",\r\n"
				+ "    \"password\":\"root\"\r\n"
				+ "}").
		when().post("rest/auth/1/session").
		then().log().all().assertThat().statusCode(200).extract().response().asString();
		
		JsonPath ses = new JsonPath(sessionTokenResponse);
		SessionName = ses.getString("session.name");
		SessionToken = ses.getString("session.value");
		System.out.println(SessionName+"="+SessionToken);
	}
	
	@Test(priority=1,enabled=false)
	public void CreateIssue() {
		
		String createIssueResponse = given().log().all().header("Content-Type","application/json").header("Cookie",SessionName+"="+SessionToken).
		body("{\r\n"
				+ "    \"fields\":{\r\n"
				+ "        \"project\":{\r\n"
				+ "            \"key\":\"RES\"\r\n"
				+ "        },\r\n"
				+ "        \"summary\":\"Credit Card card Defect Coming RestAssured\",\r\n"
				+ "        \"description\":\"This Credit Card Defect is coming from RestAssured\",\r\n"
				+ "        \"issuetype\":{\r\n"
				+ "            \"name\":\"Bug\"\r\n"
				+ "        }\r\n"
				+ "    }\r\n"
				+ "}").
		when().post("rest/api/2/issue").
		then().log().all().assertThat().statusCode(201).extract().response().asString();
		JsonPath issue = new JsonPath(createIssueResponse);
		issueId = issue.getString("id");
		System.out.println("Issue Id: "+issueId);
		
	}
	
	@Test(priority=2,enabled=false)
	public void AddComment() {
		String addCommentResponse = given().log().all().pathParam("issueId",issueId ).header("Content-Type","application/json").header("Cookie",SessionName+"="+SessionToken).
		body("{\r\n"
				+ "    \"body\":\"This comment is coming from RestAssured\",\r\n"
				+ "    \"visibility\":{\r\n"
				+ "        \"type\":\"role\",\r\n"
				+ "        \"value\":\"Administrators\"\r\n"
				+ "    }\r\n"
				+ "}").
		when().post("rest/api/2/issue/{issueId}/comment").
		then().log().all().assertThat().statusCode(201).extract().response().asString();
		JsonPath comment = new JsonPath(addCommentResponse);
		commentId = comment.getString("id");
		System.out.println("Comment ID: "+commentId);
	}

}
