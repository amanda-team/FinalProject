
package com.project.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.project.dto.MemberDTO;
import com.project.dto.OrderDTO;
import com.project.dto.ShopBoardDTO;
import com.project.paging.ShopPaging;
import com.project.service.OrderService;
import com.project.service.ShopBoardService;

@Controller
@RequestMapping("/shopboard")
public class ShopBoardController {

	@Autowired
	private HttpSession session;

	@Autowired
	private ShopBoardService sService;

	@Autowired
	private OrderService oService;

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private ShopPaging sPaging;

	@RequestMapping("/shopBoardGo")
	public String ShopBoardGo(String page) {

		int currentPage = Integer.parseInt(page);
		int lastPage = currentPage * 12;
		sPaging.sPaging(currentPage);
		List<ShopBoardDTO> boardList = sService.ShopBoardList(lastPage);
		request.setAttribute("boardList", boardList);
		System.out.println(boardList.get(0).getShop_imagepath1());
		return "shopBoard/shopBoard";
	}

	@RequestMapping(value = "/shopBoardScroll", produces = "application/text; charset=utf8")
	@ResponseBody
	public String ShopBoard_ScrollList(String page) {
		int currentPage = Integer.parseInt(page);
		int lastPage = currentPage * 12;
		sPaging.sPaging(currentPage);
		List<ShopBoardDTO> boardList = sService.ShopBoardList(lastPage);
		return new Gson().toJson(boardList);

	}

	@RequestMapping("/ShopBoard_write")
	public String ShopBoard_WriteMove() {

		return "/shopBoard/shopBoard_write";
	}

	@RequestMapping("/ShopBoardViewProc")
	public String ShopBoardSelectProc(String seq) {
		int shop_seq = Integer.parseInt(seq);
		ShopBoardDTO dto = sService.ShopBoardIdSelect(shop_seq);
		request.setAttribute("dto", dto);

		return "/shopBoard/shopBoard_view";
	}

	@RequestMapping("/ShopBoardInsertProc")
	public String filetest(ShopBoardDTO dto, List<MultipartFile> shop_images, String shop_expiration) {
		List<String> fileArrayPath = new ArrayList();
		System.out.println(dto.getShop_seq());
		System.out.println("내용: " + dto.getShop_contents());
		System.out.println("브랜드: " + dto.getShop_brand());
		System.out.println("타이틀: " + dto.getShop_title());
		System.out.println("지역: " + dto.getShop_location());
		System.out.println("유통기한: " + dto.getShop_expiration());
		System.out.println("test" + shop_expiration);
		int fileCount = 0;
		for (MultipartFile image : shop_images) {

			// if(fileCount>2){System.out.println("이미지는 최대 3개까지만 업로드 가능.");break;}

			if (image.getSize() != 0) {
				String originFileName = image.getOriginalFilename();
				long fileSize = image.getSize();
				System.out.println("originFileName : " + originFileName);
				System.out.println("fileSize : " + fileSize);
				String resourcePath = session.getServletContext().getRealPath("/resources/img/shopfoodimg/");
				System.out.println(resourcePath);
				String targetFile = resourcePath + "/" + UUID.randomUUID().toString().replace("-", "").substring(0, 8)
						+ "_foodimage.png";
				try {

					File f = new File(targetFile);
					image.transferTo(f);
					fileArrayPath.add("/img/shopfoodimg/" + f.getName());
					fileCount++;

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		if (fileCount == 1) {
			System.out.println("이미지가 하나만 들어왔을 경우");
			fileArrayPath.add("/img/default.jpg");
			fileArrayPath.add("/img/default.jpg");
			dto.setShop_imagepath1(fileArrayPath.get(0));
			dto.setShop_imagepath2(fileArrayPath.get(1));
			dto.setShop_imagepath3(fileArrayPath.get(2));

		} else if (fileCount == 2) {
			System.out.println("이미지가 2개가 들어왔을 경우");
			fileArrayPath.add("/img/default.jpg");
			dto.setShop_imagepath1(fileArrayPath.get(0));
			dto.setShop_imagepath2(fileArrayPath.get(1));
			dto.setShop_imagepath3(fileArrayPath.get(2));

		} else if (fileCount > 2) {
			System.out.println("이미지가 3개가 들어왔을 경우");
			dto.setShop_imagepath1(fileArrayPath.get(0));
			dto.setShop_imagepath2(fileArrayPath.get(1));
			dto.setShop_imagepath3(fileArrayPath.get(2));

		}

		MemberDTO mdto = (MemberDTO) session.getAttribute("id");
		// dto.setShop_id(mdto.getMember_id());
		dto.setShop_id("kkjangel");
		int result = sService.ShopBoardInsert(dto);
		return "redirect:../home";
	}

	@RequestMapping("/shopBoard_buyProc")
	public String buyProc(String quantity, int shop_seq) {
		int quantity1 = Integer.parseInt(quantity);

		MemberDTO mdto = (MemberDTO) session.getAttribute("id");

		ShopBoardDTO sdto = sService.ShopBoardIdSelect(shop_seq);
		OrderDTO dto = new OrderDTO();
		sdto.setShop_quantity(quantity1);
		// 임의로 값 더해놓게함 바꿔야댐
		int price = sdto.getShop_price();
		int result = quantity1 * price;

		dto.setProducts_seq(shop_seq); // 주문정보 seq
		dto.setSeller_id(sdto.getShop_id()); // 판매자 id
		dto.setBuyer_id(mdto.getMember_id()); // 구매자 id	
		dto.setProducts_title(sdto.getShop_title()); // 상품제목
		dto.setProducts_location(sdto.getShop_location()); // 상품내용
		dto.setProducts_price(result); // 총 구매가격
		dto.setProducts_quantity(quantity1); // 총 수량
		dto.setProducts_imagepath(sdto.getShop_imagepath1()); // 상품 이미지
		int result1 = oService.orderInsert(dto);

		System.out.println(result1);
		return "/shopBoard/shopBoard_buy";
	}

	// 아임포트 api
	// @RequestMapping("/ShopBoard_import")
	// public String importAPI() {
	//
	// }
}
