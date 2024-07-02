package cm.nzock;

import cm.platform.backoffice.AbstractIsisApplication;
import org.springframework.boot.SpringApplication;

//@Import(WebSiteConfig.class)
public class Application extends AbstractIsisApplication
{
    public static void main(String[] args){
        SpringApplication.run(Application.class ,args);
    }
}
