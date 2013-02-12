package controllers;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import play.libs.OAuth;
import play.libs.WS;
import play.libs.XPath;
import play.mvc.Controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Novae
 * Date: 11/02/13
 * Time: 17:09
 * To change this template use File | Settings | File Templates.
 */
public class LinkedIn extends Controller {
    private static OAuth.ServiceInfo LINKEDIN_SERVICE = new OAuth.ServiceInfo(
        "https://api.linkedin.com/uas/oauth/requestToken",
        "https://api.linkedin.com/uas/oauth/accessToken",
        "http://api.linkedin.com/uas/oauth/authenticate",
        "vfpkgpqp4agn",
        "MQcsUJZ7Vm3xk8ji"
    );

    private static String USER_TOKEN = "0aafc360-7562-4e31-a176-d89aa34becb5";
    private static String USER_SECRET = "7c52482c-dc45-405a-9c76-7bfa688c871c";

    public static void people() {

        Map<String, String[]> allPersons = new HashMap<String, String[]>();

//        Document doc = WS.url("http://api.linkedin.com/v1/people-search?keywords=Java&country-code=fr&postal-code=" + 56000 + "&start=" + 0)
//            .oauth(LINKEDIN_SERVICE, USER_TOKEN, USER_SECRET)
//            .get()
//            .getXml();

//        peopleSearchByPostalCode(allPersons, "56000", 0);

        for(String postalCode : getPostalCodes()) {
            System.out.println("\n\nPostal code : " + postalCode);

            peopleSearchByPostalCode(allPersons, "java", postalCode, 0);
        }

        System.out.println("Nb of persons : " + allPersons.size());

        renderXml(allPersons);
//        renderXml(doc);
    }


//    public static void


    /**
     * Méthode permettant de récupérer tous les codes postaux stockés dans le fichier postal_codes.txt
     *
     * @return Les codes postaux uniques
     */
    private static Set<String> getPostalCodes() {
        Set<String> postalCodes = new HashSet<String>();
        FileReader fr = null;
        BufferedReader r = null;

        try {
            fr = new FileReader("postal_codes.txt");
            r = new BufferedReader(fr);

            String s = null;

            while((s = r.readLine()) != null) {
                postalCodes.add(s);
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


    /**
     * Méthode permettant d'effectuer un appel WebService RESTful sur l'API de LinkedIn afin de chercher des personnes
     * en fonction de mots clés et d'un code postal. Si la requête peut retourner plus de résultats que ce qui est
     * effectivement retourné par la requête courante (la page courante), une nouvelle requête est exécutée pour les
     * mêmes mots clé et le même code postal, afin de récupérer tous les résultats.
     *
     * @param allPersons Map contenant toutes les personnes récupérées. La clé étant l'ID de la personne
     * @param keywords Mots clé à rechercher
     * @param postalCode Le code postal de la ville dans laquelle effectuer la recherche
     * @param start La position de départ des résultats à récupérer (10 pour une page)
     *
     * @return Les personnes récupérées
     */
    private static Map<String, String[]> peopleSearchByPostalCode(Map<String, String[]> allPersons, String keywords, String postalCode, Integer start) {
        Document doc = WS.url("http://api.linkedin.com/v1/people-search?keywords=" + keywords + "&country-code=fr&postal-code=" + postalCode + "&start=" + start)
            .oauth(LINKEDIN_SERVICE, USER_TOKEN, USER_SECRET)
            .get()
            .getXml();

        List<Node> persons = XPath.selectNodes("person", doc.getElementsByTagName("people").item(0));

        NamedNodeMap attrPeople = doc.getElementsByTagName("people").item(0).getAttributes();
        Integer nbTotal = (attrPeople.getNamedItem("total") != null ? Integer.valueOf(attrPeople.getNamedItem("total").getTextContent()) : 0);
        Integer startCurrent = (attrPeople.getNamedItem("start") != null ? Integer.valueOf(attrPeople.getNamedItem("start").getTextContent()) : 0);
        Integer count = (attrPeople.getNamedItem("count") != null ? Integer.valueOf(attrPeople.getNamedItem("count").getTextContent()) : null);

        for (Node p : persons) {
            String id = XPath.selectText("id", p);
            String firstName = XPath.selectText("first-name", p);
            String lastName = XPath.selectText("last-name", p);

            allPersons.put(id, new String[]{firstName, lastName});
        }

        System.out.println("nbTotal : " + nbTotal);
        System.out.println("startCurrent : " + startCurrent);
        System.out.println("count : " + count);

        if(count != null && count != 0 && startCurrent + count < nbTotal)
            return peopleSearchByPostalCode(allPersons, keywords, postalCode, (startCurrent + count));
        else
            return allPersons;
    }
}
