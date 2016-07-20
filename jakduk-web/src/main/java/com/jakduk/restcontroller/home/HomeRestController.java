package com.jakduk.restcontroller.home;

import com.jakduk.exception.ServiceError;
import com.jakduk.exception.ServiceException;
import com.jakduk.exception.SuccessButNoContentException;
import com.jakduk.model.db.Encyclopedia;
import com.jakduk.restcontroller.EmptyJsonResponse;
import com.jakduk.restcontroller.home.vo.HomeResponse;
import com.jakduk.service.CommonService;
import com.jakduk.service.HomeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.LocaleResolver;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Objects;

/**
 * @author pyohwan
 * 16. 3. 20 오후 9:13
 */

@Api(tags = "홈", description = "홈 관련")
@RestController
@RequestMapping("/api")
public class HomeRestController {

    @Resource
    LocaleResolver localeResolver;

    @Autowired
    private CommonService commonService;

    @Autowired
    private HomeService homeService;

    @ApiOperation(value = "백과사전 가져오기", produces = "application/json", response = Encyclopedia.class)
    @RequestMapping(value = "/home/encyclopedia", method = RequestMethod.GET)
    public Encyclopedia getEncyclopedia(@RequestParam(required = false) String lang,
                                        HttpServletRequest request) {

        Locale locale = localeResolver.resolveLocale(request);
        String language = commonService.getLanguageCode(locale, lang);

        Encyclopedia encyclopedia = homeService.getEncyclopedia(language);

        if (Objects.isNull(encyclopedia))
            throw new ServiceException(ServiceError.NOT_FOUND);

        return encyclopedia;
    }

    @ApiOperation(value = "홈에서 보여줄 각종 최근 데이터 가져오기", produces = "application/json", response = HomeResponse.class)
    @RequestMapping(value = "/home/latest", method = RequestMethod.GET)
    public HomeResponse dataLatest(@RequestParam(required = false) String lang,
                           HttpServletRequest request) {

        Locale locale = localeResolver.resolveLocale(request);
        String language = commonService.getLanguageCode(locale, lang);

        HomeResponse response = new HomeResponse();
        response.setPosts(homeService.getBoardLatest());
        response.setUsers(homeService.getUsersLatest(language));
        response.setGalleries(homeService.getGalleriesLatest());
        response.setComments(homeService.getBoardCommentsLatest());
        response.setHomeDescription(homeService.getHomeDescription());

        return response;
    }
}
