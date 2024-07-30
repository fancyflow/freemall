package com.zqswjtu.freemall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zqswjtu.freemall.product.vo.CategoryLevelTwoVo;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zqswjtu.common.utils.PageUtils;
import com.zqswjtu.common.utils.Query;

import com.zqswjtu.freemall.product.dao.CategoryDao;
import com.zqswjtu.freemall.product.entity.CategoryEntity;
import com.zqswjtu.freemall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        // 组装成父子的树形结构
        // 1、首先找到一级分类，一级分类的父菜单id为0
        List<CategoryEntity> topLevelMenu = entities.stream()
                .filter((categoryEntity) -> categoryEntity.getParentCid() == 0)
                .peek((categoryEntity -> categoryEntity.setChildren(getChildMenus(categoryEntity.getCatId(), entities))))
                .collect(Collectors.toList());
        // 2、二级分类以及后续的三、四等等级分类都有自己对应的父类菜单
        return topLevelMenu;
    }

    @Override
    public void removeMenuByIds(List<Long> list) {
        // TODO 1、检查当前删除的菜单是否被别的地方引用
        // 逻辑删除
        baseMapper.deleteBatchIds(list);
    }

    @Override
    public Long[] getCatelogPathById(Long catId) {
        long id = catId;
        List<Long> list = new ArrayList<>();
        do {
            list.add(id);
            CategoryEntity entity = baseMapper.selectById(id);
            id = entity.getParentCid();
        } while (id != 0);
        Collections.reverse(list);
        return list.toArray(new Long[0]);
    }

    @Override
    public List<CategoryEntity> getLevelOneCategories() {
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    @Override
    public Map<String, List<CategoryLevelTwoVo>> getCategoryJson() {
        // 1、加入缓存逻辑，缓存中保存的数据为JSON格式
        String catelogJson = redisTemplate.opsForValue().get("catelogJson");
        if (StringUtils.isEmpty(catelogJson)) {
            // 2、如果缓存没有则查数据库
            // Map<String, List<CategoryLevelTwoVo>> categoryJsonFromDb = getCategoryJsonFromDb();
            // 3、将数据转为json放入redis中缓存
            // redisTemplate.opsForValue().set("catelogJson", JSON.toJSONString(categoryJsonFromDb));
            return getCategoryJsonFromDbWithLocalLock();
        }
        // 4、将redis中的json数据转化为对象返回
        return JSON.parseObject(catelogJson, new TypeReference<Map<String, List<CategoryLevelTwoVo>>>(){});
    }

    public Map<String, List<CategoryLevelTwoVo>> getCategoryJsonFromDbWithRedissonLock() {
        // 1、使用Redisson中的分布式锁实现
        return null;
    }

    public Map<String, List<CategoryLevelTwoVo>> getCategoryJsonFromDbWithRedisLock() {
        // 1、使用redis的原生命令实现分布式锁
        String token = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", token, 300, TimeUnit.SECONDS);
        if (lock) {
            Map<String, List<CategoryLevelTwoVo>> categoryJson = null;
            System.out.println("线程" + Thread.currentThread().getName() + "获取锁");
            try {
                // 1、判断缓存是否存在，如果在直接返回
                String catelogJson = redisTemplate.opsForValue().get("catelogJson");
                if (!StringUtils.isEmpty(catelogJson)) {
                    categoryJson = JSON.parseObject(catelogJson, new TypeReference<Map<String, List<CategoryLevelTwoVo>>>(){});
                } else {
                    // 2、如果不在缓存中，则先查询数据库，然后将值放入缓存中
                    categoryJson = getCategoryJsonFromDb();
                    redisTemplate.opsForValue().set("catelogJson", JSON.toJSONString(categoryJson));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // lua脚本解锁：保证比较锁和解锁是一个原子操作
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), token);
            }
            return categoryJson;
        } else {
            // 失败可以选择过段时间重试，因为上述命令是非阻塞式的
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCategoryJsonFromDbWithRedisLock();
        }
    }

    // 从数据库查询分类菜单
    public Map<String, List<CategoryLevelTwoVo>> getCategoryJsonFromDbWithLocalLock() {
        // 类似单例模式进行双重检查，防止因synchronized阻塞的线程被重新调度时再次去查数据库获得数据
        // TODO 如果是分布式的话需要使用分布式锁
        synchronized (this) {
            String catelogJson = redisTemplate.opsForValue().get("catelogJson");
            if (!StringUtils.isEmpty(catelogJson)) {
                return JSON.parseObject(catelogJson, new TypeReference<Map<String, List<CategoryLevelTwoVo>>>(){});
            }
            Map<String, List<CategoryLevelTwoVo>> categoryJson = getCategoryJsonFromDb();
            redisTemplate.opsForValue().set("catelogJson", JSON.toJSONString(categoryJson));
            return categoryJson;
        }
    }

    public Map<String, List<CategoryLevelTwoVo>> getCategoryJsonFromDb() {
        // 1、查出所有一级分类
        List<CategoryEntity> levelOneCategories = getLevelOneCategories();
        // 2、根据一级分类查询出所有的子菜单
        Map<String, List<CategoryLevelTwoVo>> collect = levelOneCategories.stream().collect(Collectors.toMap(key -> key.getCatId().toString(), value -> {
            List<CategoryEntity> categoryLevelTwoEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", value.getCatId()));
            List<CategoryLevelTwoVo> categoryLevelTwoVos = null;
            if (categoryLevelTwoEntities != null) {
                categoryLevelTwoVos = categoryLevelTwoEntities.stream().map(item -> {
                    CategoryLevelTwoVo categoryLevelTwoVo = new CategoryLevelTwoVo(value.getCatId().toString(), null, item.getCatId().toString(), item.getName());
                    List<CategoryEntity> categoryLevelThreeEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", item.getCatId()));
                    if (categoryLevelThreeEntities != null) {
                        List<CategoryLevelTwoVo.CategoryLevelThreeVo> categoryLevelThreeVos = categoryLevelThreeEntities
                                .stream()
                                .map(categoryLevelThreeEntity -> new CategoryLevelTwoVo.CategoryLevelThreeVo(item.getCatId().toString(), categoryLevelThreeEntity.getCatId().toString(), categoryLevelThreeEntity.getName()))
                                .collect(Collectors.toList());
                        categoryLevelTwoVo.setCategoryLevelThreeList(categoryLevelThreeVos);
                    }
                    return categoryLevelTwoVo;
                }).collect(Collectors.toList());
            }
            return categoryLevelTwoVos;
        }));
        return collect;
    }

    // 递归查询所有子菜单的子菜单
    private List<CategoryEntity> getChildMenus(Long parentCid, List<CategoryEntity> entities) {
        assert parentCid != null;
        List<CategoryEntity> childMenus = entities.stream()
                .filter((categoryEntity) -> categoryEntity.getParentCid().equals(parentCid))
                .peek((childMenu) -> childMenu.setChildren(getChildMenus(childMenu.getCatId(), entities)))
                .collect(Collectors.toList());
        return childMenus;
    }
}