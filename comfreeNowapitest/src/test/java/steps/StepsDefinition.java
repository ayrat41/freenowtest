package steps;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.testng.annotations.Test;
import util.Util;

import java.util.ArrayList;
import java.util.List;


public class StepsDefinition extends Util {
// Create params acceptable to all functions
    static Response response;
    static int userId;
    static List<Integer> postIds;

// With this function user call /user api compare "name" value with defined name in config.properties
    @Test
    public void find_userId_contains_username_use_to_the_next_scenario() {
        //Read parametrs from config file and create  endpoint uri
        String url = Util.prop.getProperty("URL");
        String api = Util.prop.getProperty("usersUri");
        String endPointUri = url+api;
        System.out.println(endPointUri);
        String name = Util.prop.getProperty("username");
        //Get request to endpoint assert Code
        response = RestAssured.given().get(endPointUri);
        response.then().assertThat().statusCode(200);
        JsonPath jsonPathEvaluator = response.jsonPath();
        //Store all usernames in List Run through the list
        List <String> userNames  = jsonPathEvaluator.getList("username");
        for (int i=0; i<userNames.size(); i++) {
            if (userNames.get(i).trim().equalsIgnoreCase(name)) {
                System.out.println("Defined user for testing " + userNames.get(i).trim());
                //find userId for defined username
                userId = jsonPathEvaluator.getInt("id["+i+"]");
                break;
            }
        }
    }

    @Test(dependsOnMethods = { "find_userId_contains_username_use_to_the_next_scenario" })
    public void find_posts_contains_userId_use_to_the_next_scenario() {
        //Read parametrs from config file and create  endpoint uri
        String url = Util.prop.getProperty("URL");
        String api = Util.prop.getProperty("postsUri");
        String endPointUri = url+api;
        System.out.println(endPointUri);
        String name = Util.prop.getProperty("userId");
        //Get request to endpoint assert Code
        response = RestAssured.given().when().get(endPointUri + "?userId=" + userId);
        response.then().assertThat().statusCode(200);
        JsonPath jsonPathEvaluator = response.jsonPath();
        //Initiate postId ArrayList and store all postId for defined user in the List
        postIds = new ArrayList<Integer>();
        postIds.addAll(jsonPathEvaluator.getList("id"));
        System.out.println("List of postIds for defined user created");
    }


// With this function we call /comments api using userId parameter from previos method
// Get all emails from comments for this specific user
// Check if email syntax is valid using method in Util class
    @Test (dependsOnMethods = { "find_posts_contains_userId_use_to_the_next_scenario" })
    public void find_comments_contains_userId_check_emails() {
        //Read parametrs from config file and create  endpoint uri
        String url = Util.prop.getProperty("URL");
        String api = Util.prop.getProperty("commentsUri");
        String endPointUri = url + api;
        System.out.println(endPointUri);
        //Get request to endpoints with param postId from the previous List assert Code
        //and store all userEmails from comments
        List<String> commentemails = new ArrayList<String>();
        for(int i =0; i<this.postIds.size(); i++) {
            response = RestAssured.given().when().get(endPointUri + "?postId=" + postIds.get(i));
            response.then().assertThat().statusCode(200);
            JsonPath jsonPathEvaluator = response.jsonPath();
            commentemails.addAll(jsonPathEvaluator.getList("email"));
        }
        //Assert if email is valid using isValid method from Util classs
            int status = 0;
            for (int n = 0; n < commentemails.size(); n++) {
                if (!Util.isValid(commentemails.get(n))) {
                    System.out.println("The email ==> " + commentemails.get(n) + " is not valid");
                    status = status + 1;
                } else {
                    System.out.println(n+". The email ==> " + commentemails.get(n) + " is valid");
                }
            }
            if (status == 0) {
                System.out.println("All emails for defined user are valid");

        }
    }

}

