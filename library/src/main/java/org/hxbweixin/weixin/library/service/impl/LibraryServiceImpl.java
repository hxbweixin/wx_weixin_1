package org.hxbweixin.weixin.library.service.impl;

import java.util.LinkedList;

import org.hxbweixin.weixin.library.domain.Book;
import org.hxbweixin.weixin.library.domain.DebitList;
import org.hxbweixin.weixin.library.service.LibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LibraryServiceImpl implements LibraryService{

	@Autowired
	private BookRepository bookRepository;

	@Override
	public Page<Book> search(String keyword, int pageNumber) {

		// 创建分页条件，页码从外面（页面）传入，每页固定最多显示3条数据
		Pageable pageable = PageRequest.of(pageNumber, 3);

		Page<Book> page;
		if (StringUtils.isEmpty(keyword)) {
			// 没有查询的关键字
			// where disabled == false
			page = this.bookRepository.findByDisabledFalse(pageable);
		} else {
			// 有关键字，要根据关键字来查询数据
			// where name like '%' + keyword + '%' and disabled == false
			page = this.bookRepository.findByNameContainingAndDisabledFalse(keyword, pageable);
		}

		return page;
	}

	@Override
	public void add(String id, DebitList list) {
		if (list.getBooks() == null) {
			list.setBooks(new LinkedList<>());
		}

		// 1.判断id对应的图书是否在借阅列表中。
		boolean exists = false;
		for (Book book : list.getBooks()) {
			if (book.getId().equals(id)) {
				exists = true;
				break;
			}
		}
		if (!exists) {
			this.bookRepository.findById(id)
					.ifPresent(list.getBooks()::add);

		}
	}

	@Override
	public void remove(String id, DebitList list) {
		list.getBooks()
				// 把集合转换为Stream
				.stream()
				.filter(book -> book.getId().equals(id))
				// 找到过滤后的第一个结果
				.findFirst()
				// 把找到的图书，从集合里面删除
				.ifPresent(list.getBooks()::remove);

	}
}

