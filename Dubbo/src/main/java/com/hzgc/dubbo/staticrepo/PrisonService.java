package com.hzgc.dubbo.staticrepo;

import java.sql.SQLException;

public interface PrisonService {
    /**
     * 用来更新人员位置，
     * @param prisonSearchOpts，对应的是pkeysUpdate 参数
     * @return 0,表示更新成功，1，表示失败
     */
    int updateLocation(PrisonSearchOpts prisonSearchOpts) throws SQLException;

    /**
     * 用来重置人员位置
     * @param prisonSearchOpts 对应的是pkeysReset
     * @return 0,表示重置成功，1，表示失败
     */
    int resetLocation(PrisonSearchOpts prisonSearchOpts);

    /**
     * 用来获取对象类型下，各个位置的人的个数
     * @param prisonSearchOpts 对应的是pkeysCount
     * @return PrisonCountResults, 封装好的结果。
     */
    PrisonCountResults countByLocation(PrisonSearchOpts prisonSearchOpts);

    /**
     * 重置所有对象库的人员信息
     * @return 0,表示重置成功，1，表示失败
     */
    int resetLocation();
}
