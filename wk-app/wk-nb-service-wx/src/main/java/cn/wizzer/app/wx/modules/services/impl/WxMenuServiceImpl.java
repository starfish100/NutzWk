package cn.wizzer.app.wx.modules.services.impl;

import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.wx.modules.models.Wx_menu;
import cn.wizzer.app.wx.modules.services.WxMenuService;
import com.alibaba.dubbo.config.annotation.Service;
import org.nutz.aop.interceptor.ioc.TransAop;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;

@IocBean(args = {"refer:dao"})
@Service(interfaceClass=WxMenuService.class)
public class WxMenuServiceImpl extends BaseServiceImpl<Wx_menu> implements WxMenuService {
    public WxMenuServiceImpl(Dao dao) {
        super(dao);
    }

    /**
     * 新增菜单
     *
     * @param menu
     * @param pid
     */
    @Aop(TransAop.READ_COMMITTED)
    public void save(Wx_menu menu, String pid) {
        String path = "";
        if (!Strings.isEmpty(pid)) {
            Wx_menu pp = this.fetch(pid);
            path = pp.getPath();
        } else pid = "";
        menu.setPath(getSubPath("wx_menu", "path", path));
        menu.setParentId(pid);
        dao().insert(menu);
        if (!Strings.isEmpty(pid)) {
            this.update(Chain.make("hasChildren", true), Cnd.where("id", "=", pid));
        }
    }

    /**
     * 级联删除菜单
     *
     * @param menu
     */
    @Aop(TransAop.READ_COMMITTED)
    public void deleteAndChild(Wx_menu menu) {
        dao().execute(Sqls.create("delete from wx_menu where path like @path").setParam("path", menu.getPath() + "%"));
        if (!Strings.isBlank(menu.getParentId())) {
            int count = count(Cnd.where("parentId", "=", menu.getParentId()));
            if (count < 1) {
                dao().execute(Sqls.create("update wx_menu set hasChildren=0 where id=@pid").setParam("pid", menu.getParentId()));
            }
        }
    }
}
