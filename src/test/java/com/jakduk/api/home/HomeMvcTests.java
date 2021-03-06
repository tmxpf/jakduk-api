package com.jakduk.api.home;

import com.jakduk.api.TestMvcConfig;
import com.jakduk.api.common.Constants;
import com.jakduk.api.common.board.category.BoardCategory;
import com.jakduk.api.common.board.category.BoardCategoryGenerator;
import com.jakduk.api.common.util.JakdukUtils;
import com.jakduk.api.common.util.ObjectMapperUtils;
import com.jakduk.api.model.db.Encyclopedia;
import com.jakduk.api.model.db.HomeDescription;
import com.jakduk.api.model.embedded.ArticleStatus;
import com.jakduk.api.model.embedded.CommonWriter;
import com.jakduk.api.model.simple.ArticleSimple;
import com.jakduk.api.model.simple.UserSimple;
import com.jakduk.api.restcontroller.HomeRestController;
import com.jakduk.api.restcontroller.vo.board.BoardGallerySimple;
import com.jakduk.api.restcontroller.vo.home.HomeArticle;
import com.jakduk.api.restcontroller.vo.home.HomeArticleComment;
import com.jakduk.api.restcontroller.vo.home.HomeGallery;
import com.jakduk.api.restcontroller.vo.home.HomeLatestItemsResponse;
import com.jakduk.api.service.ArticleService;
import com.jakduk.api.service.GalleryService;
import com.jakduk.api.service.HomeService;
import com.jakduk.api.service.UserService;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(HomeRestController.class)
@Import(TestMvcConfig.class)
@AutoConfigureRestDocs(outputDir = "build/snippets")
public class HomeMvcTests {

    @Autowired
    private MockMvc mvc;

    @MockBean private RestTemplateBuilder restTemplateBuilder;

    @MockBean private HomeService homeService;
    @MockBean private UserService userService;
    @MockBean private ArticleService articleService;
    @MockBean private GalleryService galleryService;

    private BoardCategory boardCategory;
    private CommonWriter commonWriter;

    @Before
    public void setUp() {
        commonWriter = CommonWriter.builder()
                .userId("571ccf50ccbfc325b20711c5")
                .username("test07")
                .providerId(Constants.ACCOUNT_TYPE.JAKDUK)
                .build();

        List<BoardCategory> categories = BoardCategoryGenerator.getCategories(Constants.BOARD_TYPE.FOOTBALL, JakdukUtils.getLocale());

        boardCategory = categories.get(0);
    }

    @Test
    @WithMockUser
    public void getEncyclopediaWithRandomTest() throws Exception {

        Encyclopedia expectEncyclopedia = Encyclopedia.builder()
                .id("5427972b31d4c583498c19bf")
                .seq(2)
                .kind("player")
                .language("ko")
                .subject("박성화")
                .content("슈퍼리그 출범 원년의 MVP이다. 수비수임에도 탁월한 득점력을 갖췄던 박성화는 후기리그부터 주장을 맡아 팀 분위기를 쇄신했다. 넓은 시야를 바탕으로 공수에서 맹활약한 박성화는 할렐루야 우승의 견인차 역할을 했다.")
                .build();

        when(homeService.getEncyclopediaWithRandom(anyString()))
                .thenReturn(expectEncyclopedia);

        mvc.perform(
                get("/api/home/encyclopedia")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(ObjectMapperUtils.writeValueAsString(expectEncyclopedia)))
                .andDo(
                        document("get-random-encyclopedia",
                                responseFields(
                                        fieldWithPath("id").type(JsonFieldType.STRING).description("백과사전 ID"),
                                        fieldWithPath("seq").type(JsonFieldType.NUMBER).description("백과사전 번호"),
                                        fieldWithPath("kind").type(JsonFieldType.STRING).description("종류 [player,book]"),
                                        fieldWithPath("language").type(JsonFieldType.STRING).description("언어 [ko,en]"),
                                        fieldWithPath("subject").type(JsonFieldType.STRING).description("제목"),
                                        fieldWithPath("content").type(JsonFieldType.STRING).description("내용")
                                )
                        ));
    }

    @Test
    @WithMockUser
    public void getLatestItemsTest() throws Exception {

        HomeDescription homeDescription = HomeDescription.builder()
                .id("55875f3784ae8ca220de4956")
                .desc("<h4>알림판</h4>\\n<a href=\\\"https://jakduk.com/board/free/890\\\">2017년 4월 사이트 업데이트 사항</a>")
                .priority(5)
                .build();

        List<HomeGallery> galleries = Arrays.asList(
                HomeGallery.builder()
                        .id("58b9050b807d714eaf50a111")
                        .name("사진찍기")
                        .writer(commonWriter)
                        .imageUrl("https://dev-api.jakduk.com//gallery/thumbnail/58b9050b807d714eaf50a111")
                        .thumbnailUrl("https://dev-api.jakduk.com//gallery/thumbnail/58b9050b807d714eaf50a111")
                        .build()
        );

        List<UserSimple> users = Arrays.asList(
                UserSimple.builder()
                        .id("571ccf50ccbfc325b20711c5")
                        .username("test07")
                        .about("안녕하세요. 반갑습니다.")
                        .build()
        );

        HomeArticle article = HomeArticle.builder()
                .id("59c8879fa2b594c5d33e6ac4")
                .seq(2)
                .status(new ArticleStatus(false, false))
                .board(Constants.BOARD_TYPE.FOOTBALL.name())
                .category(boardCategory.getCode())
                .writer(commonWriter)
                .subject("글 제목입니다.")
                .views(15)
                .galleries(
                        Arrays.asList(
                                new BoardGallerySimple("58b9050b807d714eaf50a111", "https://dev-api.jakduk.com//gallery/thumbnail/58b9050b807d714eaf50a111")
                        ))
                .shortContent("짧은 내용입니다. (100자)")
                .build();

        ArticleSimple articleSimple = new ArticleSimple();
        BeanUtils.copyProperties(article, articleSimple);

        List<HomeArticle> articles = Arrays.asList(article);

        List<HomeArticleComment> comments = Arrays.asList(
                HomeArticleComment.builder()
                        .id("54b5058c3d96b205dc7e2809")
                        .article(articleSimple)
                        .writer(commonWriter)
                        .content("댓글 내용입니다.")
                        .build()
        );

        when(homeService.getHomeDescription())
                .thenReturn(homeDescription);

        when(userService.findSimpleUsers())
                .thenReturn(users);

        when(articleService.getLatestComments())
                .thenReturn(comments);

        when(articleService.getLatestArticles())
                .thenReturn(articles);

        when(galleryService.findSimpleById(nullable(ObjectId.class), anyInt()))
                .thenReturn(galleries);

        HomeLatestItemsResponse response = HomeLatestItemsResponse.builder()
                .homeDescription(homeDescription)
                .users(users)
                .comments(comments)
                .articles(articles)
                .galleries(galleries)
                .build();

        mvc.perform(
                get("/api/home/latest")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(ObjectMapperUtils.writeValueAsString(response)))
                .andDo(document("get-home-latest-items",
                        responseFields(
                                fieldWithPath("homeDescription").type(JsonFieldType.OBJECT).description("알림판"),
                                fieldWithPath("homeDescription.id").type(JsonFieldType.STRING).description("알림판 ID"),
                                fieldWithPath("homeDescription.desc").type(JsonFieldType.STRING).description("알림판 내용"),
                                fieldWithPath("homeDescription.priority").type(JsonFieldType.NUMBER).description("알림판 우선순위"),
                                fieldWithPath("users").type(JsonFieldType.ARRAY).description("최근 가입 회원 목록"),
                                fieldWithPath("users.[].id").type(JsonFieldType.STRING).description("회원 ID"),
                                fieldWithPath("users.[].username").type(JsonFieldType.STRING).description("회원 이름"),
                                fieldWithPath("users.[].about").type(JsonFieldType.STRING).description("회원 소개"),
                                fieldWithPath("articles").type(JsonFieldType.ARRAY).description("최근글 목록"),
                                fieldWithPath("articles.[].id").type(JsonFieldType.STRING).description("글 ID"),
                                subsectionWithPath("articles.[].status").type(JsonFieldType.OBJECT).description("글 상태"),
                                fieldWithPath("articles.[].seq").type(JsonFieldType.NUMBER).description("글 번호"),
                                fieldWithPath("articles.[].board").type(JsonFieldType.STRING).description("게시판"),
                                fieldWithPath("articles.[].category").type(JsonFieldType.STRING).description("말머리"),
                                subsectionWithPath("articles.[].writer").type(JsonFieldType.OBJECT).description("글쓴이"),
                                fieldWithPath("articles.[].subject").type(JsonFieldType.STRING).description("글 제목"),
                                fieldWithPath("articles.[].views").type(JsonFieldType.NUMBER).description("읽음 수"),
                                subsectionWithPath("articles.[].galleries.[]").type(JsonFieldType.ARRAY).description("그림 목록"),
                                fieldWithPath("articles.[].shortContent").type(JsonFieldType.STRING).description("본문 100자"),
                                fieldWithPath("comments").type(JsonFieldType.ARRAY).description("최근 댓글 목록"),
                                fieldWithPath("comments.[].id").type(JsonFieldType.STRING).description("댓글 ID"),
                                subsectionWithPath("comments.[].article").type(JsonFieldType.OBJECT).description("연동 글"),
                                subsectionWithPath("comments.[].writer").type(JsonFieldType.OBJECT).description("글쓴이"),
                                fieldWithPath("comments.[].content").type(JsonFieldType.STRING).description("댓글 내용 (110자)"),
                                fieldWithPath("galleries").type(JsonFieldType.ARRAY).description("최근 사진 목록"),
                                fieldWithPath("galleries.[].id").type(JsonFieldType.STRING).description("사진 ID"),
                                fieldWithPath("galleries.[].name").type(JsonFieldType.STRING).description("사진 이름"),
                                subsectionWithPath("galleries.[].writer").type(JsonFieldType.OBJECT).description("사진 올린이"),
                                fieldWithPath("galleries.[].imageUrl").type(JsonFieldType.STRING).description("큰 사진 URL"),
                                fieldWithPath("galleries.[].thumbnailUrl").type(JsonFieldType.STRING).description("작은 사진 URL")
                        )
                ));
    }
}
