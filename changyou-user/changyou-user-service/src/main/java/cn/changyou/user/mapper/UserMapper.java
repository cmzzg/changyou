package cn.changyou.user.mapper;

import cn.changyou.user.pojo.User;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author xgl
 * @create 2019-12-27 0:34
 */
public interface UserMapper extends Mapper<User> {
    @Select("select count(1) from cy_user")
    Integer queryUserCount();
}
