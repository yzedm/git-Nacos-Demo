# nacos服务发现

![image-20251222155707470](images/media/image-20251222155707470.png)

## http远程调用

原先的远程http请求调用代码:

```java
 Set<Long> itemIds = vos.stream().map(CartVO::getItemId).collect(Collectors.toSet());
        // 2.查询商品
        //利用RestTemplate发起http请求，得到http的响应
        ResponseEntity<List<ItemDTO>> response = restTemplate.exchange(
                "http://localhost:8081/items?ids={ids}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ItemDTO>>() {},
                Map.of("ids", CollUtil.join(itemIds, ","))
        );
        //这里的http请求使用的是硬编码，之后万一有更多的购物车服务的话，
        // 此处无法完成服务的正常使用，此时就需要使用到注册中心！！
        //解析响应
        if(!response.getStatusCode().is2xxSuccessful()){
            //查询失败，直接结束
            return;
        }
        List<ItemDTO> items = response.getBody();
        if (CollUtils.isEmpty(items)) {
            return;
        }
```

## Nacos服务发现

利用nacos服务发现新代码：

```java
 // 1.获取商品id
        Set<Long> itemIds = vos.stream().map(CartVO::getItemId).collect(Collectors.toSet());
        //1.根据服务名称获取服务实例列表
        List<ServiceInstance> instances = discoveryClient.getInstances("item-service");
        if (CollUtils.isEmpty(instances)){
            return;
        }
        //2.负载均衡，从实例列表中挑选一个实例
        ServiceInstance serviceInstance = instances.get(RandomUtil.randomInt(instances.size()));
        //3.利用RestTemplate发起Http请求，得到http响应
        ResponseEntity<List<ItemDTO>> response = restTemplate.exchange(
                serviceInstance.getUri()+"items?ids={ids}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ItemDTO>>() {},
                Map.of("ids", CollUtil.join(itemIds, ","))
        );
        if(!response.getStatusCode().is2xxSuccessful()){
            //查询失败，直接结束
            return;
        }
        List<ItemDTO> items = response.getBody();
        if (CollUtils.isEmpty(items)) {
            return;
        }
```

****

# OpenFeign



由于服务发现中的Http代码较多，使用nacos和之前没有进行服务拆分的代码相比代码复杂了很多。

## 简介

![image-20251222162607506](images/media/image-20251222162607506.png)

原本的代码的主要功能：

![image-20251222162745812](images/media/image-20251222162745812.png)

## 入门

 ![image-20251222162944738](images/media/image-20251222162944738.png)

![image-20251222163400084](images/media/image-20251222163400084.png)

1.<img src="images/media/image-20251222164235504.png" alt="image-20251222164235504" style="zoom:50%;" />

2.<img src="images/media/image-20251222164359242.png" alt="image-20251222164359242" style="zoom:50%;" />

3.<img src="images/media/image-20251222164419849.png" alt="image-20251222164419849" style="zoom:50%;" />

4.<img src="images/media/image-20251222164450842.png" alt="image-20251222164450842" style="zoom:50%;" />

## 连接池优化

![image-20251223101006821](images/media/image-20251223101006821.png)

开启步骤

![image-20251223101629433](images/media/image-20251223101629433.png)

## Feign最佳实践

在添加商品到购物车中的功能中需要使用到查询商品功能，当时定义了一个OpenFeign的接口，在订单创建时也会使用到查询商品功能，就还需要再重复的定义一个查询商品的OpenFeign接口吗？

![image-20251223102152996](images/media/image-20251223102152996.png)

此时有了代码的重复的问题，后续还需要重复修改

方案一：

<img src="images/media/image-20251223102535683.png" alt="image-20251223102535683" style="zoom: 33%;" />

项目结构复杂。

方案二：

<img src="images/media/image-20251223102956171.png" alt="image-20251223102956171" style="zoom:33%;" />

耦合度比较高。

这里演示第二种

CartApplication启动类所在的包是spring自动扫描的地方(com.ym.cart)，所以尽管pom中添加了client的依赖，但是spring无法创建client的实例，因为扫描的范围只有启动类所在的位置。

注意:

> ![image-20251223104350398](images/media/image-20251223104350398.png)

## 日志输出

![image-20251223105656403](images/media/image-20251223105656403.png)

![image-20251223110022123](images/media/image-20251223110022123.png)

![image-20251223110506844](images/media/image-20251223110506844.png)

# 网关

拆分微服务带来的问题：

> 1.服务地址过多，而且后期可能发生变化，前端不知道访问谁;
>
> 2.每个服务都需要登录信息，如果每个服务中添加的话就会导致代码冗余;

解决方案：
网关：是网络的关口，负责请求的路由，转发，身份校验 

使用的是springcloud的 spring cloud gateway

## 入门

>1.创建新模块
>
>2.引入网关依赖
>
>3.编写启动类
>
>4.配置路由规则

![image-20251224171834419](images/media/image-20251224171834419.png)

## 路由属性

路由断言

![image-20251225150236083](images/media/image-20251225150236083.png)

路由过滤器

![image-20251225150249916](images/media/image-20251225150249916.png)

例子：添加请求头过滤器

```F#
    gateway:
      routes:
        - id: item # 路由规则id，自定义，唯一
          uri: lb://item-service # 路由的目标服务，lb代表负载均衡，会从注册中心拉取服务列表
          predicates: # 路由断言，判断当前请求是否符合当前规则，符合则路由到目标服务
            - Path=/items/**,/search/** # 这里是以请求路径作为判断规则
          filters:
            - AddRequestHeader= truth, anyone long-press like button will rich
```

```java
    @ApiOperation("分页查询商品")
    @GetMapping("/page")
    public PageDTO<ItemDTO> queryItemByPage(PageQuery query,@RequestHeader(value = "truth" , required = false)String truth) {
        System.out.println(truth);
        // 1.分页查询
        Page<Item> result = itemService.page(query.toMpPage("update_time", false));
        // 2.封装并返回
        return PageDTO.of(result, ItemDTO.class);
    }
```

![image-20251225151420018](images/media/image-20251225151420018.png)

控制台将添加的请求头打印，如果需要每个路由都生效该过滤器，则将该过滤器添加到route同级的标签下

```javascript
spring:
  application:
    name: gateway
  cloud:
    nacos:
      server-addr: 192.168.237.128:8848
    gateway:
      routes(5):
      default-filters:
        - AddRequestHeader= truth, anyone long-press like button will rich
```

## 登录校验功能

​	首先登录授权微服务通过jwt的方式将用户的token保存到前端，别的微服务如订单和购物车都需要获取该token，不希望将jwt秘钥传递给这些微服务，现在通过网关来进行登录校验！向后传递登录token。

 网关处理流程：

![image-20251225152655205](images/media/image-20251225152655205.png)

![image-20251225153130319](images/media/image-20251225153130319.png)

### 自定义过滤器 (GloableFilter)

​	将自定义过滤器作为判断用户身份的功能，并且保证该过滤器在NettyRoutingFilter之前执行，就能确保在转发路由请求前登录校验。

1.创建自己的过滤器

```java
@Component
public class MyGlobalFilter  implements GlobalFilter{
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //TODO 模拟登录校验逻辑
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        System.out.println("headers"+headers);
        //放行
        return chain.filter(exchange);
    }
}
```

2.设置过滤器优先级

```SAS
通过实现的Ordered的类的getOrder方法，该方法返回的数字越小，优先级越大，如 NettyRoutingFilter的优先级为：
```

![image-20251225155128182](images/media/image-20251225155128182.png)

在debug模式下先进入了Gloable中：

![image-20251225162403507](images/media/image-20251225162403507.png)

### 自定义过滤器(GatewayFilter)

![image-20251225162751877](images/media/image-20251225162751877.png)

此处跳过

### 实现登录校验（GloableFilter）

需求：在网关中基于过滤器实现登录校验功能

```
通过继承GloableFilter，Ordered实现，AuthLoginGloableFilter
代码:AuthLoginGloableFilter.java
```

### 实现登录用户信息传递功能

```
//todo 传递用户信息
String userInfo = userid.toString();
exchange.mutate()
        .request(builder->builder.header("user-info",userInfo))
        .build();
```

![image-20251226153603388](images/media/image-20251226153603388.png)

注意需要传递给下一个过滤器。

### 将保存用户信息功能放入hm-common中

由于每个微服务都有可能有获取登录用户的需求，所以将该功能定义在common模块中

拦截器定义：

```java
public class UserInfoInterceptor implements HandlerInterceptor {
    //该拦截器不做任何的登录拦截功能，唯一的作用就是保存登录成功的user用户信息

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //在进入controller前执行，所以他的作用是保存用户信息
        //1.获取登录用户信息
        String header = request.getHeader("user-info");
        //2.判断是否获取了用户，存入ThreadLocal
        if (StrUtil.isNotBlank(header)) {
            UserContext.setUser(Long.valueOf(header));
        }
        //3.放行
            return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //controller之后执行，清理用户信息
        UserContext.removeUser();

    }}
```

拦截器配置

```java
@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //此处不添加任何 拦截path，表示不做任何的拦截
        registry.addInterceptor(new UserInfoInterceptor());
    }
}
注意：此时该配置类无法被spring扫描到！！！，因为项目中的各种微服务都不会扫描commnon中的配置类
```

需要再下面的配置文件中，将配置类添加进去，保证项目启动时，该配置类被spring扫描到;

![image-20251229144934567](images/media/image-20251229144934567.png)

```xml
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  com.hmall.common.config.MyBatisConfig,\
  com.hmall.common.config.MvcConfig,\
  com.hmall.common.config.JsonConfig
```

但是此时重新启动gateway还是会报错：

![image-20251229145914854](images/media/image-20251229145914854.png)

网关部分代码报错，WebMvcconfigurer找不到？

​	是因为Gateway中pom.xml引用了hm-common的依赖，而WebMvcConfigurer是springmvc包下的，但是网关gateway的底层不是基于springmvc的一套，所以里面没有springmvc，所以报错了。

![image-20251229151908634](images/media/image-20251229151908634.png)

解决方法：

让common在微服务中生效，在网关中不生效，利用网关中没有springmvc的特性,条件注解

```java
@Configuration
@ConditionalOnClass(DispatcherServlet.class)
/*ConditionalOnClass 该注解的作用是，如果该环境下没有springmvc的核心类DispatcherServlet的话
* 就不会自动将该MvcConfig加载，主要的作用是网关(没有springmvc)部分需要加载该类(有springmvc)*/
public class MvcConfig  implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //该拦截器不会拦截任何请求，只是为了传递Userinfo对象
        registry.addInterceptor(new UserInfoInterceptor());
    }
}
```

### OpneFeign传递用户

微服务之间的项目调用，也需要传递用户的信息
