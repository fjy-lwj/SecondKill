<%--
  Created by IntelliJ IDEA.
  User: fjy11
  Date: 2020/6/29
  Time: 19:33
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>秒杀页面</title>
</head>
<body>
    <form action="${pageContext.request.contextPath}/sk/doSecondKill" method="post">
        <input type="hidden" name="id" value="1001">
        <input type="button" value="点击参与秒杀">
    </form>
    <script src="jquery/jquery-2.1.1.min.js" type="text/javascript"></script>
    <script src="layer/layer.js"></script>
    <script type="text/javascript">
        $("form :button").click(function () {
            $.ajax({
                url:$("form").prop("action"),
                type:"post",
                data:$("form").serialize(),
                success:function (res) {
                    if (res == "ok") {
                        layer.msg("秒杀成功");
                    } else {
                        layer.msg(res);
                    }
                }
            });
        });

    </script>
</body>
</html>
