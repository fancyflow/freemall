package com.zqswjtu.freemall.product.web;

import com.zqswjtu.freemall.product.entity.CategoryEntity;
import com.zqswjtu.freemall.product.service.Car;
import com.zqswjtu.freemall.product.service.CategoryService;
import com.zqswjtu.freemall.product.vo.CategoryLevelTwoVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    @Qualifier("bmwCar")
    private Car car;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {

        // TODO 查出所有的一级分类并在页面面展示
        List<CategoryEntity> levelOneCategories = categoryService.getLevelOneCategories();

        // 视图解析器进行拼串
        // "classpath:/templates/" + 返回值 + ".html"
        model.addAttribute("levelOneCategories", levelOneCategories);
        return "index";
    }

    @ResponseBody
    @GetMapping("/test")
    public String test() {
        return car.run();
    }

    @ResponseBody
    @GetMapping("/index/catelog.json")
    public Map<String, List<CategoryLevelTwoVo>> getCatelogJson() {
        return categoryService.getCategoryJson();
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        /**
         * Redisson的分布式锁和JUC包中的Lock方法使用方式相同，但是JUC包中的Lock都是本地锁，
         * Redisson中的分布式锁可以在分布式情况下使用
         * 分布式环境下锁的名字相同，获取的就是同一把锁
         */
        RLock lock = redissonClient.getLock("lock");
        /**
         * Redisson中有一个看门狗机制，即使用不带过期参数的lock方法时，它有30s的默认过期时间(可以配置)
         * 但是看门狗机制会定时给锁延长过期时间，即每过一段时间都看门狗机制会将锁的过期时间延长，
         * 定时检查的间隔为默认过期时间 / 3，延长的过期时间为默认过期时间(即过期时间每次加上默认过期时间)
         * 这样既可防止redis节点宕机造成死锁，又可以使开发人员不用担心在业务执行期间就把锁提早释放
         */
        lock.lock();
        // 如果使用的是带过其参数的lock方法，则没有看门狗机制，即锁会到时自动过期，便会引发一些安全隐患
        // 例如，业务执行时间较长，还没执行完锁便超时释放了，这时等到业务执行完后释放锁便会出现问题
        // 因为该线程的锁已经超时自动释放了，这时释放的锁就很有可能是其他线程获取的锁，就会引发异常
        // lock.lock(10, TimeUnit.SECONDS);
        try {
            System.out.println("线程" + Thread.currentThread().getName() + "获取锁");
            // 模拟业务执行
            Thread.sleep(20000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("线程" + Thread.currentThread().getName() + "释放锁");
            lock.unlock();
        }
        return "hello";
    }
}
