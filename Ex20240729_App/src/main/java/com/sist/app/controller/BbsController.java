package com.sist.app.controller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;

import com.sist.app.service.BbsService;
import com.sist.app.util.FileRenameUtil;
import com.sist.app.util.Paging2;
import com.sist.app.vo.BbsVO;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;



@Controller
public class BbsController {
    @Autowired
    BbsService b_service;

    @Autowired
    private ServletContext application;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${server.upload.path}")
    private String upload_path;

	private String editor_img = "/resources/editor_img"; // 썸머노트 이미지 추가할 때 저장할 위치


    @RequestMapping("list")
    public ModelAndView list(@RequestParam String bname, String searchType, String searchValue, String cPage) {
        ModelAndView mv = new ModelAndView();
        int nowPage = 1;

        if(cPage != null){
            nowPage = Integer.parseInt(cPage);
        }

        if(bname == null || bname.trim().length()==0){
            bname = "bbs";
        }

        int totalRecord = b_service.getCount(bname, searchType, searchValue);

        // 위에서 전체 게시물의 수를 얻었다면 이제 페이징 기법을
        // 사용하는 객체를 생성할 수 있다.
        Paging2 page = new Paging2(7, 5, totalRecord, nowPage, bname);

        nowPage = page.getNowPage();

        // 페이징기법의 HTML코드를 얻어낸다.
        String pageCode = page.getSb().toString();

        // 뷰 페이지에서 표현할 목록 가져오기
        int begin = page.getBegin();
        int end = page.getEnd();

        BbsVO[] b_ar = b_service.getList(bname, searchType, searchValue, begin, end);

        // 뷰 페이지에서 표현하고자 하는 값들 저장
        mv.addObject("b_ar", b_ar);
        mv.addObject("b_page", page);
        mv.addObject("pageCode", pageCode);
        mv.addObject("totalRecord", totalRecord);
        mv.addObject("numPerPage", page.getNumPerPage());
        mv.addObject("nowPage", nowPage);
        mv.addObject("bname", bname);
        mv.setViewName(bname+"/list");

        return mv;
    }


    @GetMapping("write")
    public String write(String bname, String cPage) {
        return bname+"/write";
    }

    @PostMapping("write")
    public ModelAndView write(BbsVO bvo) {
        ModelAndView mv = new ModelAndView();

        String fname = "";

        MultipartFile file = bvo.getFile();
        if(file != null && file.getSize()>0){
            String realPath = application.getRealPath(upload_path);

            try {
                fname = file.getOriginalFilename();
                bvo.setOri_name(fname);

                fname = FileRenameUtil.checkSameFileName(fname, realPath);

                file.transferTo(new File(realPath, fname));
                bvo.setFile_name(fname);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        String ip = request.getRemoteAddr();
        bvo.setIp(ip);

        b_service.add(bvo);

        mv.setViewName("redirect:/list?bname="+bvo.getBname());
        return mv;
    }

    @PostMapping("saveImg")
    @ResponseBody
    public Map<String, String> saveImg(MultipartFile s_file) {
        Map<String, String> i_map = new HashMap<>();
        String fname = null;

        if(s_file != null && s_file.getSize()>0){
            String realPath = application.getRealPath(editor_img);
            fname = s_file.getOriginalFilename();

            fname = FileRenameUtil.checkSameFileName(fname, realPath);

            try {
                s_file.transferTo(new File(realPath, fname));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String c_path = request.getContextPath();
        i_map.put("url",c_path + editor_img);
        i_map.put("fname",fname);

        return i_map;
    }
    
    
}
