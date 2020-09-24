var jQuery = require("jquery");

jQuery(function($) {
    "use strict";

    (function() {
        const currentUserUrl = $('.mysite-sign-in-buttons').data('current-user-url'),
            signIn = $('[href="#sign-in"]'),
            signOut = $('[href="#sign-out"]'),
            greetingLabel = $('#mysiteGreetingLabel'),
            greetingText = greetingLabel.text(),
            body = $('body');
        if(currentUserUrl){
            $.getJSON(currentUserUrl + "?nocache=" + new Date().getTime(), function(currentUser) {
                const isAnonymous = 'anonymous' === currentUser.authorizableId;

                if(isAnonymous) {
                    signIn.show();
                    body.addClass('anonymous');
                } else {
                    signOut.show();
                    greetingLabel.text(greetingText + ", " + currentUser.name);
                    greetingLabel.show();
                }
            });
        }
    })();
});
