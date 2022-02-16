package com.wayapaychat.paymentgateway.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
	
	@GetMapping("/callback")
    public String main(Model model, String input) {
		System.out.println("HELLO");
		//String HTMLSTring = "<!DOCTYPE html>" + "<html>" + "<head>" + "<title>JSoup Example</title>" + "</head>" + "<body>" + "<table><tr><td> <h1>HelloWorld</h1></tr>" + "</table>" + "</body>" + "</html>";
        String mInput = "<!DOCTYPE html>\r\n"
        		+ " \r\n"
        		+ "<html>\r\n"
        		+ "<head>\r\n"
        		+ "    <meta name=\"viewport\" content=\"width=device-width\" />\r\n"
        		+ "    <title></title>\r\n"
        		+ "</head>\r\n"
        		+ "<body>\r\n"
        		+ "        <script type=\"text/javascript\">\r\n"
        		+ "\r\n"
        		+ "            parent.location.href = 'https://mpitest.unifiedpaymentsnigeria.com/runtran.jsp?ORDERID=9838898&SESSIONID=8EF14389F218ABA683FB30BB132F065B&ReqData=88399313f888fe39d48ded864167ff87b92812a0ed150464f40f14b7e68f16ebdff262542740012570e0eedaa0d042dd06f55c23dd551a97a1930fd5d8cbe1860aa05a6b58f32303f8a4e1bf43dc313ba3e4951ee02d545e154afa7a24ad6d664076b0137e474bef39f8223ff384b8e36551a0b02bb98d387813049822764ed9b98aee507e20c029e22eed5b69a21a5c';\r\n"
        		+ "        </script>\r\n"
        		+ "<script type=\"text/javascript\" src=\"/bnith__PnMHC3ScRuZPvNiV4WRlIk7nN51d2c-_PRT1KD3DG6d_bc2zxwceqj0SwJrcnXrn\"></script> <script language=\"JavaScript\" type=\"text/javascript\">var _0x5aae=[\"cookie\",\"x-bni-fpc=\",\"; expires=Thu, 01 Jan 2037 00:00:00 UTC; path=/;\",\"x-bni-rncf=1641404792486; expires=Thu, 01 Jan 2037 00:00:00 UTC; path=/;\",\"get\"];function fiprn(){( new fiprn_v2)[_0x5aae[4]](function(_0x6130x2,_0x6130x3){document[_0x5aae[0]]= _0x5aae[1]+ _0x6130x2+ _0x5aae[2],document[_0x5aae[0]]= _0x5aae[3]})}</script><script>fiprn();</script></body>\r\n"
        		+ " \r\n"
        		+ "</html>";
        return mInput; //view
    }

}
