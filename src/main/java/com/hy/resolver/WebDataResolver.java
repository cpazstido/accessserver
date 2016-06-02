package com.hy.resolver;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * Created by cpazstido on 2016/6/1.
 */
public class WebDataResolver {
    private  static Logger logger = Logger.getLogger(WebDataResolver.class);

    public String writeXmlForGetDeviceID(String info) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("eMonitor_XML");
        root.addAttribute("EventType","GetDeviceID");
        Element eRandomCode = root.addElement("Info");
        eRandomCode.setText(info);
        return document.asXML();
    }
}
