package com.guangke.forum.controller;

import com.guangke.forum.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
public class DataController {
    @Autowired
    private DataService dataService;

    @RequestMapping(path = "/data",method = {RequestMethod.GET,RequestMethod.POST})
    public String getData(){
        return "/site/admin/data";
    }
    //查询UV
    @PostMapping(path = "/data/uv")
    public String getUv(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model){
        long uvCount = dataService.getUv(start,end);
        model.addAttribute("uvCount",uvCount);
        model.addAttribute("uvStartDate",start);
        model.addAttribute("uvEndDate",end);
        return "forward:/data";
    }

    //查询DAU
    @PostMapping(path = "/data/dau")
    public String getDau(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model){
        long uvCount = dataService.getDau(start,end);
        model.addAttribute("dauCount",uvCount);
        model.addAttribute("dauStartDate",start);
        model.addAttribute("dauEndDate",end);
        return "forward:/data";
    }
}
