# html转pdf

参考的网址：

html制作的过程：https://gitee.com/taote/bookjs-eazy#%E5%A5%87%E5%81%B6%E9%A1%B5%E5%AE%9E%E7%8E%B0

制作pdf的网址：  https://gitee.com/wuxue107/screenshot-api-server

## 1.搭建自己的html转pdf的服务器

docker：容器加载

```shell
docker pull wuxue107/screenshot-api-server

## -e MAX_BROWSER=[num] 环境变量可选，最大的puppeteer实例数，忽略选项则默认值:1 , 值auto：[可用内存]/200M
##  -e PDF_KEEP_DAY=[num] 自动删除num天之前产生的文件目录,默认0: 不删除文件
docker run -p 3000:3000 -td --rm -e MAX_BROWSER=1 -e PDF_KEEP_DAY=0 -v ${PWD}:/screenshot-api-server/public --name=screenshot-api-server wuxue107/screenshot-api-server
```

## 2.制作自己的html页面



### 3.配置下载pdf地址为自己搭建的服务器地址



## 4.如何安装自定义字体



    字体的安装：1.将字体放到docker的fonts目录下
              2.重启docker 或 进入docker shell 里执行install-font.sh
              3.docker容器了目录下/screenshot-api-server 
              进入容器里安装后，执行下fc-list 看下安装成功没
              出现字体信息就成功：root@fe59563de8db:/screenshot-api-server# fc-list
    /usr/share/fonts/truetype/liberation/LiberationSansNarrow-Italic.ttf: Liberation Sans Narrow:style=Italic
    
    
    制作页面：注意字体的名字要一致
                @font-face {
            font-family: "SourceHanSansCN-Light";
            src: url(/fonts/SourceHanSansCN-Light.otf);
            font-weight: 400;
            font-style: normal
        	}
        	
        .pendant-title p {
            line-height: 50px;
            font-size: 24px;
            margin: 2mm 0 0 10mm;
            color: #000;
           font-family: "SourceHanSansCN-Light";
        }

