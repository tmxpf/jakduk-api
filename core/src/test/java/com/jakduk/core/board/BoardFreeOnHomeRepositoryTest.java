package com.jakduk.core.board;

import com.jakduk.core.common.CommonConst;
import com.jakduk.core.model.simple.BoardFreeOnHome;
import com.jakduk.core.repository.board.free.BoardFreeOnHomeRepository;
import com.jakduk.core.util.AbstractSpringTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collections;

/**
 * Created by pyohwan on 16. 10. 30.
 */
public class BoardFreeOnHomeRepositoryTest extends AbstractSpringTest {

    @Autowired
    private BoardFreeOnHomeRepository sut;

    @Test
    public void findAll() {
        Sort sort = new Sort(Sort.Direction.DESC, Collections.singletonList("seq"));
        Pageable pageable = new PageRequest(0, CommonConst.HOME_SIZE_LINE_NUMBER, sort);

        Page<BoardFreeOnHome> freeOnHomePage = sut.findAll(pageable);

        Assert.assertTrue(freeOnHomePage.hasContent());
    }
}
