package org.hxbweixin.weixin.library.comtroller;

import org.hxbweixin.weixin.library.domain.DebitList;
import org.hxbweixin.weixin.library.service.LibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.context.request.WebRequest;

@Controller
@RequestMapping("/weixin_1/library/debit")
@SessionAttributes({ "debitList" })
public class DebitController {

	@Autowired
	private LibraryService libraryService;

	@RequestMapping
	public String debit(@RequestParam("id") String id, WebRequest request) {

		DebitList list = (DebitList) request.getAttribute("debitList", WebRequest.SCOPE_SESSION);
		// 必须要保证list不能为null
		if (list == null) {
			list = new DebitList();
			request.setAttribute("debitList", list, WebRequest.SCOPE_SESSION);
		}

		this.libraryService.add(id, list);

		// 添加完成以后，重定向到借阅列表显示的页面
		return "redirect:/weixin_1/library/debit/list";
	}

	@RequestMapping("list")
	public String list() {
		return "/WEB-INF/views/library/debit/list.jsp";
	}
	@RequestMapping("remove/{id}")
	public String remove(@PathVariable("id") String id,
			// 从Session里面或名为debitList的对象，并且自动强制类型转换
			@SessionAttribute("debitList") DebitList list) {
		this.libraryService.remove(id, list);
		return "redirect:/weixin_1/library/debit/list";
	}
}

