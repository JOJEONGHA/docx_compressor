package bookDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import bookVO.Authors;
import bookVO.Books;
import bookVO.JoinTable;
import bookVO.Pieces;

public class BooksDAO {
	private Connection con;
	private PreparedStatement pstmt;
	private DataSource dataFactory;

	public BooksDAO() {
		try {
			Context ctx = new InitialContext();
			Context envContext = (Context) ctx.lookup("java:/comp/env");
			dataFactory = (DataSource) envContext.lookup("jdbc/mariaDB");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// (info_UD - 책 번호 검색)/ *번호검색
	public JoinTable bookInfo(String num) {
		JoinTable info = new JoinTable();

		List<JoinTable> list = new ArrayList<JoinTable>();
		list = listBooks();
		
		for (JoinTable i : list) {
			String temp = i.getBooknum();
			if (temp.equals(num)) {
				info = i; 
				break;
			}
		}
		return info;
	}

	// (info_UD - 선택한 정보 검색(return List))
	public List<JoinTable> bookSearch(String num, int value) {
		List<JoinTable> info = new ArrayList<JoinTable>();

		List<JoinTable> list = new ArrayList<JoinTable>();
		list = listBooks();
		
		String temp = new String();
		for (JoinTable i : list) {
			switch(value) {
			case 0:	// 도서설명
				temp = i.getSummary();
				break;
			case 1:	// 출판사
				temp = i.getPublisher();
				break;
			case 2:	// 도서번호
				temp = i.getBooknum();
				break;
			case 3:	// 저자
				temp = i.getAuthorname();
				break;
			case 4:	// 책 제목
				temp = i.getTitle();
				break;
			}
			
			if (temp.contains(num)) {
				info.add(i); 
			}
		}
		return info;
	}

	// (Main - 책 리스트)
	public List<JoinTable> listBooks() {
		List<JoinTable> list = new ArrayList<JoinTable>();
		try {
			con = dataFactory.getConnection();
			String query = "SELECT * " + "FROM books b " + "INNER JOIN pieces p ON b.booknum = p.booknum "
					+ "inner join authors a on p.authornum = a.authornum " + "order by b.title asc";
			pstmt = con.prepareStatement(query);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				String booknum = rs.getString("booknum");
				String title = rs.getString("title");
				String authorname = rs.getString("authorname");
				String publisher = rs.getString("publisher");
				String summary = rs.getString("summary");
				String authornum = rs.getString("authornum");
				String birthyear = rs.getString("birthyear");
				JoinTable jt = new JoinTable();

				// Same authors catch
				while (true) {
					boolean switch_ = rs.next();

					if (switch_ == true) {
						String temp = rs.getString("booknum");
						if (booknum.equals(temp)) {
							authorname += ", " + rs.getString("authorname");
							authornum += ", " + rs.getString("authornum");
						} else {
							rs.previous();
							break;
						}
					} else {
						break;
					}

				}
				jt.setBooknum(booknum);
				jt.setTitle(title);
				jt.setAuthorname(authorname);
				jt.setPublisher(publisher);
				jt.setSummary(summary);
				jt.setAuthornum(authornum);
				jt.setBirthyear(birthyear);
				list.add(jt);
			}
			rs.close();
			pstmt.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	// (create_C - 책 추가 시 일련번호 생성)
	public String firstCreate() {
		Date today = new Date();
		SimpleDateFormat date = new SimpleDateFormat("yyMMddhhmmss");
		String now = date.format(today);
		// book code is start 01-
		String booknum = "01-" + now;

		return booknum;
	}

	// (Create_C - 해당 정보의 작가 번호 return)
	public String whatAuthor(String name, String birth) {
		String num = new String();
		try {
			con = dataFactory.getConnection();
			String query = "SELECT * FROM authors" + " WHERE authorname = '" + name + "' and birthyear = '" + birth
					+ "'";
			pstmt = con.prepareStatement(query);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next() == false)
				return null;
			num = rs.getString("authornum");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return num;
	}

	// (enroll_C - 같은 이름,생일 가진 작가 유무 판단 후 없으면 db에 추가) *작가생성
	public boolean checkAuthor(Authors a) {
		String temp_name = a.getAuthorname();
		String temp_birth = a.getBirthyear();
		String query = "SELECT * FROM authors WHERE authorname= '" + temp_name + "' and birthyear= '" + temp_birth
				+ "'";

		try {
			con = dataFactory.getConnection();
			pstmt = con.prepareStatement(query);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next() == true)
				return false;

			// Authors Code Setting
			// Authors code is start 00-
			Date today = new Date();
			SimpleDateFormat date = new SimpleDateFormat("yyMMddhhmmss");
			String now = date.format(today);
			String authornum = "00-" + now;

			// Add Author into DB
			query = "INSERT INTO authors ";
			query += " (authornum,authorname,birthyear)";
			query += " values(?,?,?)";
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, authornum);
			pstmt.setString(2, temp_name);
			pstmt.setString(3, temp_birth);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return true;
	}

	// 도서정보 수정 *도서갱신
	public void UpdateBooks(Books b){
		// Books(num,pub,sum) DB에 업데이트
		// 해당 책(num)을 찾아서, 변경(pub,sum)
		try {
			con = dataFactory.getConnection();
			String pub = b.getPublisher();
			String sum = b.getSummary();
			String num = b.getBooknum();
			num = num.trim();
			String query = "UPDATE books SET publisher = '"
							+ pub +"' , summary = '"
							+ sum +"' WHERE booknum = '"+ num +"'";
			pstmt = con.prepareStatement(query);	
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// *도서삭제
	public void deleteBooks(JoinTable j) {
		try {
			con = dataFactory.getConnection();
			// JoinTable 에서 Booknum, authornum 도출
			String booknum = j.getBooknum();
			// authornum 분리 작업 필요
			String[] authornum = j.getAuthornum().split(",");

			// Delete 수행
			String query;
			for (String temp : authornum) {
				// piece delete
				query = "DELETE FROM pieces WHERE booknum = '" + booknum + "'AND authornum = '" + temp + "'";
				pstmt = con.prepareStatement(query);
				pstmt.executeUpdate();
			}
			// book delete
			query = "DELETE FROM books WHERE booknum ='" + booknum + "'";
			pstmt = con.prepareStatement(query);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	// *도서추가
	public void createBooks(Books b, List<Pieces> l) {
		try {
			con = dataFactory.getConnection();

			// book insert into DB
			String num = b.getBooknum();
			String title = b.getTitle();
			String publisher = b.getPublisher();
			String summary = b.getSummary();
			String query = "INSERT INTO books";
			query += " (booknum,title,publisher,summary)";
			query += " values(?,?,?,?)";
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, num);
			pstmt.setString(2, title);
			pstmt.setString(3, publisher);
			pstmt.setString(4, summary);
			pstmt.executeUpdate();

			// pieces insert into DB
			for (Pieces p : l) {
				query = "INSERT INTO pieces";
				query += " (booknum,authornum)";
				query += " values(?,?)";
				pstmt = con.prepareStatement(query);
				pstmt.setString(1, p.getBooknum());
				pstmt.setString(2, p.getAuthornum());
				pstmt.executeUpdate();
			}
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
}