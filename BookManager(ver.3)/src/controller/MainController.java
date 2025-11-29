package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bookDAO.BooksDAO;
import bookVO.Authors;
import bookVO.Books;
import bookVO.JoinTable;
import bookVO.Pieces;

@WebServlet("/controller/*")
public class MainController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private BooksDAO dao = new BooksDAO();

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doHandle(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doHandle(request, response);
	}

	private void doHandle(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String nextPage = null;
		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		String action = request.getPathInfo(); // URL name of request

		// (Main - create new book)
		if (action.equals("/Create_Book.do")) {
			String visit = request.getParameter("btn_create");
			// First visit and Direct visit(URL)
			if (visit.equals("first_visit") || visit == null) {
				String booknum = dao.firstCreate();
				request.setAttribute("booknum", booknum);
				nextPage = "/create_C.jsp";
			} else {
				String booknum = request.getParameter("Bnum_info");
				String title = request.getParameter("Bname_info");
				String[] author = request.getParameterValues("Bauth_info"); // 작가가 String[] 형태로 저장되어있다. 홍길동/12314
				String publisher = request.getParameter("Bpub_info");
				String summary = request.getParameter("Binf_info");

				// String 배열 구분자로 구별
				String[] authorname = new String[author.length];
				String[] authorBirth = new String[author.length];
				int i = 0;
				for (String str : author) {
					String[] temp = str.split("/");
					authorname[i] = temp[0];
					authorBirth[i++] = temp[1];
				}
				// 구별된 배열 바탕으로 authornum 구한다.
				String[] authornum = new String[author.length];
				for (i = 0; i < author.length; i++) {
					authornum[i] = dao.whatAuthor(authorname[i], authorBirth[i]);
				}

				// Books, Pieces를 db에 Update
				// Books, Pieces Object 생성
				Books bookTemp = new Books(booknum, title, publisher, summary);
				List<Pieces> pieceTemp = new ArrayList<Pieces>();
				Pieces[] p = new Pieces[author.length];
				for (int j = 0; j < author.length; j++) {
					p[j] = new Pieces();
					p[j].setBooknum(booknum);
					p[j].setAuthornum(authornum[j]);
					pieceTemp.add(p[j]);
				}

				// DAO에 전달 및 Update
				dao.createBooks(bookTemp, pieceTemp);

				nextPage = "/main_LS.jsp";
			}
		}
		// (main - Load all book list)
		else if (action.equals("/List.do")) {
			List<JoinTable> booksList = dao.listBooks();
			String value = request.getParameter("sType");
			String search = request.getParameter("search");

			if (search != null) {
				if (value.equals("Bpub")) { // 출판사,1
					booksList = dao.bookSearch(search, 1);
				} else if (value.equals("Bnum")) {// 도서번호,2
					booksList = dao.bookSearch(search, 2);
				} else if (value.equals("Bauth")) {// 저자,3
					booksList = dao.bookSearch(search, 3);
				} else if (value.equals("Bname")) {// 책 제목,4
					booksList = dao.bookSearch(search, 4);
				} else if (value.equals("Binfo")) {// 도서 설명,0
					booksList = dao.bookSearch(search, 0);
				}
			}

			request.setAttribute("booksList", booksList);
			nextPage = "/main_LS.jsp";
		}
		// (info - book information update & delete) **정보갱신
		else if (action.equals("/update.do")) {
			String temp_num = request.getParameter("bookinfo"); // bookinfo 속 number만 존재
			JoinTable temp = dao.bookInfo(temp_num);
			// View Button Click Action
			if (request.getParameter("btnInfo").equals("view")) {
				request.setAttribute("bookInfo", temp);
				nextPage = "/info_UD.jsp";
			} else if (request.getParameter("btnInfo").equals("delete")) {
				dao.deleteBooks(temp);
				nextPage = "/main_LS.jsp";
			} else if (request.getParameter("btnInfo").equals("update")) {
				// 도서 업데이트
				String temp_Pub = request.getParameter("Bpub_info");
				String temp_Sum = request.getParameter("Binf_info");
				Books tempB = new Books(temp_num, temp_Pub, temp_Sum);
				dao.UpdateBooks(tempB);
				nextPage = "/main_LS.jsp";
			}
		}
		// (all page - back to main(list))
		else if (action.equals("/Back.do") || action == "") {
			nextPage = "/main_LS.jsp";
		}
		// (createA_C - Add author) **작가생성
		else if (action.equals("/CreateAuthor.do")) {
			String visit = request.getParameter("btn_createA");
			String booknum = request.getParameter("booknum");
			request.setAttribute("booknum", booknum);
			if (visit.equals("first")) {
				nextPage = "/enroll_C.jsp";
			} else {
				String name = request.getParameter("name_author");
				String birthyear = request.getParameter("birth_author");
				Authors temp = new Authors(name, birthyear);
				boolean exi = dao.checkAuthor(temp); // Add author or Exist Yes,No
				request.setAttribute("exist", exi);
				nextPage = "/main_LS.jsp";
			}
		}
		RequestDispatcher dispatcher = request.getRequestDispatcher(nextPage);
		dispatcher.forward(request, response);
	}
}
