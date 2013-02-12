package controllers;

import play.mvc.Controller;

public class Application extends Controller {

    public static void index() {
        int i = 1;
        System.out.println("1) i = " + i);
        i = i++;
        i++;
        System.out.println("2) i = " + i);
        render();
    }
}