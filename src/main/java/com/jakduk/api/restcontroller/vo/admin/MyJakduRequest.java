package com.jakduk.api.restcontroller.vo.admin;

import lombok.Data;

/**
 * Created by pyohwan on 16. 3. 5.
 */

@Data
public class MyJakduRequest {
    private int homeScore;
    private int awayScore;
    private String jakduScheduleId;
}
