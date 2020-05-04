package be.uantwerpen.fti.ei.Distributed.project.Node;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UnicastController {

    @RequestMapping(value = "/postIP", method = RequestMethod.PUT)
    public void test(){
        System.out.println("feest");
    }

}
