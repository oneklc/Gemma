package ubic.gemma.web.feed;

import java.util.Date;

/**
 * TODO Document Me
 * 
 * @author paul
 * @version $Id: NewsItem.java,v 1.1 2008/10/25 21:10:48 paul Exp $
 */
public class NewsItem {

    private String title;

    private Date date;

    public String getTitle() {
        return title;
    }

    public void setTitle( String title ) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate( Date date ) {
        this.date = date;
    }

    public String getTeaser() {
        return teaser;
    }

    public void setTeaser( String teaser ) {
        this.teaser = teaser;
    }

    public String getBody() {
        return body;
    }

    public void setBody( String body ) {
        this.body = body;
    }

    private String teaser;

    private String body;

}
