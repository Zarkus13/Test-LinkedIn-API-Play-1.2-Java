package controllers;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import play.libs.OAuth;
import play.libs.WS;
import play.libs.XPath;
import play.mvc.Controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Application extends Controller {
    private static OAuth.ServiceInfo LINKEDIN_SERVICE = new OAuth.ServiceInfo(
        "https://api.linkedin.com/uas/oauth/requestToken",
        "https://api.linkedin.com/uas/oauth/accessToken",
        "http://api.linkedin.com/uas/oauth/authenticate",
        "vfpkgpqp4agn",
        "MQcsUJZ7Vm3xk8ji"
    );

    private static String USER_TOKEN = "0aafc360-7562-4e31-a176-d89aa34becb5";
    private static String USER_SECRET = "7c52482c-dc45-405a-9c76-7bfa688c871c";

    public static void index() {

        Map<String, String[]> allPersons = new HashMap<String, String[]>();

        for(Integer postalCode : getPostalCodes()) {
            System.out.println("Postal code : " + postalCode);

            Document doc = WS.url("http://api.linkedin.com/v1/people-search?keywords=Java&country-code=fr&postal-code=" + postalCode)
                .oauth(LINKEDIN_SERVICE, USER_TOKEN, USER_SECRET)
                .get()
                .getXml();

            List<Node> persons = XPath.selectNodes("person", doc.getElementsByTagName("people").item(0));
//            Integer nbResults = Integer.valueOf(XPath.selectText("people-search", doc));

            for (Node p : persons) {
                String id = XPath.selectText("id", p);
                String firstName = XPath.selectText("first-name", p);
                String lastname = XPath.selectText("last-name", p);

                allPersons.put(id, new String[]{firstName, lastname});
            }
        }

        System.out.println("Nb of persons : " + allPersons.size());

        renderXml(allPersons);
    }

    private static Set<Integer> getPostalCodes() {
        Set<Integer> postalCodes = new HashSet<Integer>();
        FileReader fr = null;
        BufferedReader r = null;

        try {
            fr = new FileReader("postal_codes.txt");
            r = new BufferedReader(fr);

            String s = null;

            while((s = r.readLine()) != null) {
                postalCodes.add(Integer.valueOf(s));
            }
        } catch (Exception e) {
            System.out.println("Problem !");
        } finally {
            try {
                fr.close();
                r.close();
            } catch (IOException e) {
                System.out.println("Boooom !");
            }
        }

        return postalCodes;
    }
}