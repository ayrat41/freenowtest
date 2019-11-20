package steps;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.testng.annotations.Test;
import util.Util;
import java.util.List;


public class StepsDefinition extends Util {

    static Response response;
    static int userId;
    static List<Integer> postIds;

// With this function user call /user api compare "name" value with defined name in config.properties
    @Test
    public void find_userId_contains_username_use_to_the_next_scenario() {
        String url = Util.prop.getProperty("URL");
        String api = Util.prop.getProperty("usersUri");
        String endPointUri = url+api;
        System.out.println(endPointUri);
        String name = Util.prop.getProperty("name");
        System.out.println(name);
        response = RestAssured.given().get(endPointUri);
        response.then().assertThat().statusCode(200);
        JsonPath jsonPathEvaluator = response.jsonPath();

        List <String> userNames  = jsonPathEvaluator.getList("username");
        for (int i=0; i<userNames.size(); i++) {
            if (userNames.get(i).trim().equalsIgnoreCase(name)) {
                System.out.println(userNames.get(i).trim());
                userId = jsonPathEvaluator.getInt("id["+i+"]");
                break;
            }
        }
    }


// With this function we call /comments api using userId parameter from previos method
// Get all emails from comments for this specific user
// Check if email syntax is valid using method in Util class
    @Test (dependsOnMethods = { "find_userId_contains_username_use_to_the_next_scenario" })
    public void find_comments_contains_userId_check_emails() {
        String url = Util.prop.getProperty("URL");
        String api = Util.prop.getProperty("commentsUri");
        String endPointUri = url + api + "?user=" + userId;
        System.out.println(endPointUri);
        response = RestAssured.given().parameter("user", userId).get(endPointUri);
        response.then().assertThat().statusCode(200);
        JsonPath jsonPathEvaluator = response.jsonPath();
        List<String> commentemails = jsonPathEvaluator.getList("email");
        int status = 0;
        for (int i=0; i<commentemails.size(); i++) {
            if (!Util.isValid(commentemails.get(i))) {
                System.out.println("The email ==> " + commentemails.get(i) + " is not valid");
                status=status+1;
            }
        }
        if (status==0){
            System.out.println("All emails for are valid");
        }
    }

}

