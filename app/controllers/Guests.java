package controllers;

import models.User;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.mvc.Controller;
import utils.IPUtils;

public class Guests extends Controller {
	
	 public static void subscribe(){
         String ipAddress = IPUtils.getIpFromRequest(request);
		 renderTemplate("Guests/subscribe.html", ipAddress);
	 }
	 
	 public static void save(@Required @Valid User user) {
        if (validation.hasErrors()) {
            flash.error("Veuillez corriger les erreurs");
            params.flash();
            validation.keep();
            subscribe();
        }
        
        user.firstName = user.firstName.substring(0, 1).toUpperCase() + user.firstName.substring(1).toLowerCase();
        user.lastName = user.lastName.toUpperCase();

        user.save();
        flash.success("Votre inscription a réussi, un administrateur doit au préalable valider votre compte pour le rendre actif.");
        Application.index();
    }
}
