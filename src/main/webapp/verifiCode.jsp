<%--
  Created by IntelliJ IDEA.
  User: fjy11
  Date: 2020/6/30
  Time: 0:55
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>验证码功能</title>
<%--   <link rel="stylesheet" href="layui/css/layui.css">--%>
</head>
<body>


    <div class="layui-form-item">
        <div class="layui-inline">
            <div class="layui-input-inline">
                <input id="phoneNum" type="tel" name="phone"  lay-verify="required|phone" placeholder="请输入手机号" autocomplete="off" class="layui-input">
            </div>
            <button type="botton" class="layui-btn layui-btn-warm" id="sendCode" lay-filter="sendCode">发送验证码</button>
        </div>
        <br>
        <div class="layui-inline">
            <div class="layui-input-inline">
                <input id="code" type="text" name="code" placeholder="请输入验证码" autocomplete="off" class="layui-input">
            </div>
            <button type="botton" class="layui-btn layui-btn-primary" id="confirmCode" lay-filter="confirmCode">确认</button>
        </div>
        <div>${error}</div>

    </div>



<script src="jquery/jquery-2.1.1.min.js" type="text/javascript"></script>
<script src="${pageContext.request.contextPath}/layui/layui.js"></script>
    <script>
        layui.use(['jquery','form','element','layer'],function () {
            var $ =layui.jquery;
            var form = layui.form;
            var element = layui.element;
            var layer = layui.layer;

            $("#sendCode").click(function () {
                var phone = $("#phoneNum").val();
                $.post("${pageContext.request.contextPath}/code/getcode",{"phone":phone},function (data) {
                    layer.msg("您的验证码为"+data+"有效期为2分钟，请尽快输入");
                });
            });

            $("#confirmCode").click(function () {
                var code = $("#code").val();
                //alert(code);
                window.location = "${pageContext.request.contextPath}/code/confirmCode?code="+code;
            });

        });




    </script>
</body>
</html>
