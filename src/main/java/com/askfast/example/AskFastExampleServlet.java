package com.askfast.example;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.askfast.askfastapi.AskFast;
import com.askfast.askfastapi.AskFastRestClient;
import com.askfast.model.Language;

public class AskFastExampleServlet extends HttpServlet {

    private static final long serialVersionUID = -1L;
    private Logger log = Logger.getLogger( AskFastExampleServlet.class.getName() );
    
    private static final String accountId = "";
    private static final String key = "";
    
    private static HashMap<String, Boolean> voteCount = new HashMap<String, Boolean>();

    @Override
    protected void service( HttpServletRequest req, HttpServletResponse resp ) 
                                throws ServletException, IOException {
        
        // Remove the first backslash
        String path = req.getPathInfo().substring( 1 );
        // Split everything from the path and place it in the parts array
        String[] parts = path.split( "\\/" );
        
        if(parts.length > 0 ) {
            
            // check if the first part of the path equals question
            if(parts[0].equalsIgnoreCase( "question" ) ) {
                
                AskFast askfast = new AskFast( req );
                
                if(parts.length == 1 || parts[1].equals( "start" )) {
                    
                    askfast.ask( "Do you like my website. Press 1 for yes and 2 for no" );
                    askfast.addAnswer( "", "/question/thankyou?result=true" );
                    askfast.addAnswer( "", "/question/thankyou?result=false" );
                    
                } else if(parts[1].equals( "thankyou" )) {
                    
                    String requester = req.getParameter( "requester" );
                    String res = req.getParameter("result");
                    Boolean result = Boolean.parseBoolean( res );
                    
                    voteCount.put( requester, result );
                    
                    if(result) {
                        askfast.say( "Thank you!" );
                    } else {
                        askfast.say( "That is to bad. I will try to improve it." );
                    }
                }
                
                askfast.setPreferredLanguage( Language.ENGLISH_GREATBRITAIN.getCode() );
                
                resp.getWriter().print(askfast.render());
            }
            else if(parts[0].equalsIgnoreCase( "startcall" ) ) {
                
                String addresses = req.getParameter( "addresses" );
                if(addresses==null) {
                    resp.sendError( HttpServletResponse.SC_BAD_REQUEST, "No address given" );
                }
                
                String[] addressArray = addresses.split( "," );
                String host = getHost( req );
                String url = host + "/question";
                AskFastRestClient client = new AskFastRestClient( accountId, key );
                // Loop through all the given addresses
                for(String address : addressArray) {
                    if(address!=null && !address.isEmpty()) {
                        try {
                            // Per address start the outbound call
                            client.startPhoneDialog( address, url );
                        } catch (Exception e) {
                            log.warning("Failed to start call: " + e.getMessage());
                        }
                    }
                }
            }
            else if(parts[0].equalsIgnoreCase( "count" ) ) {
                
                int yesCount = 0;
                int noCount = 0;
                
                for(String key : voteCount.keySet()) {
                    if(voteCount.get( key )) {
                        yesCount++;
                    } else {
                        noCount++;
                    }
                }
                    
                    resp.getWriter().write( "There are " +yesCount + " who liked your website. " + noCount + " did not." );  
                }
            }
        else {
            resp.sendError( HttpServletResponse.SC_NOT_FOUND );
        }
    }
    
    private String getHost( HttpServletRequest req ) {

        int port = req.getServerPort();
        // Remove the port number if is default http(s)
        if ( ( req.getScheme().equals( "http" ) && port == 80 ) || 
               ( req.getScheme().equals( "https" ) && port == 443 ) ) {
            port = -1;
        }
        String url = null;
        try {
            URL serverURL = new URL( req.getScheme(), req.getServerName(), port, "" );
            url = serverURL.toString();
        }
        catch ( Exception e ) {
        }
        return url;
    }
}
