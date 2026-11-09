package com.partnerportal.web;

import com.partnerportal.domain.Notice;
import com.partnerportal.service.BoardService;
import com.partnerportal.service.Categories;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @GetMapping("/notice/list")
    public String notices(@RequestParam(required = false) String keyword,
                          @PageableDefault(size = 10) Pageable pageable, Model model) {
        Page<Notice> page = boardService.notices(keyword, pageable);
        model.addAttribute("page", page);
        model.addAttribute("keyword", keyword);
        return "board/notice-list";
    }

    @GetMapping("/notice/{id}")
    public String notice(@PathVariable Long id, Model model) {
        model.addAttribute("notice", boardService.readNotice(id));
        return "board/notice-detail";
    }

    @GetMapping("/faq")
    public String faq(@RequestParam(required = false) String category, Model model) {
        model.addAttribute("faqs", boardService.faqs(category));
        model.addAttribute("categories", Categories.FAQ);
        model.addAttribute("selected", category);
        return "board/faq";
    }
}
