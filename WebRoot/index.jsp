<%-- 
    Document   : index
    Created on : Jul 4, 2017, 10:48:32 AM
    Author     : adong
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://"
			+ request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>BaiduMap Cache</title>
        <script>
            (function(){
                // remove all baidu localStorage cache
                var reg = /^BMap_/;
                if ( typeof "localStorage" !== "undefined" )
                    for ( var i = 0, ie = localStorage.length; i < ie; i++ )
                    {
                        var k = localStorage.key(i++);
                        if ( k !== null && reg.test(k) )
                            localStorage.removeItem(k);
                    }
            })();
        </script>
        
        <!--<script src="http://api.map.baidu.com/api?v=2.0&ak=BzL5c9UaDlph64beEDkDNzG3wrsNZ2b0"></script>-->
        <script src="<%=basePath%>BaiduMapCache/cache/api.map.baidu.com/api?v=2.0&ak=BzL5c9UaDlph64beEDkDNzG3wrsNZ2b0"></script>
        
        <style>
            html,body
            {
                height: 100%;
                margin: 0;
                padding: 0;
                overflow: hidden;
            }
            
            #themap 
            {
                width: 100%;
                height: 100%;
            }
            
        </style>
        
    </head>
    <body>
        <div id="themap"></div>
        <script>
            (function(){
            alert(window.location.href);
/*                 var newmap = new BMap.Map("themap", {enableMapClick: false});
                newmap.enableScrollWheelZoom(true);

                newmap.setMapStyle({
                    features: ["road", "water", "land", "building"],
                    style:"dark"
                });

                newmap.centerAndZoom(new BMap.Point(118.807347, 32.048577),13); */
                
            	var map = init_map();
                
            })();
            
            function init_map() {
                //var init_pos = {"lon":113.221156, "lat":40.791099}; //察右前旗
                var init_pos = {"lon":120.072151, "lat":43.878022}; //阿鲁科尔沁
                var init_zoom = 15;
                var newmap = new BMap.Map(document.getElementById("themap"), {enableMapClick: false});
                newmap.enableScrollWheelZoom(true);
          
            	  var styleJson = [
        				 {
        				   "featureType": "background",
        				   "elementType": "all",
        				   "stylers": {
                        "color": "#0d1b3a"
                    }
        				 },
        				 {
                  "featureType": "road",
                  "elementType": "geometry",
                  "stylers": {
                            "color": "#2E8CC2"
                  }
                  },
                  {
                  "featureType": "all",
                            "elementType": "labels.text.fill",
                            "stylers": {
                                      "color": "#000000"
                            }
                  }
        			 ]
          
                newmap.setMapStyle({
                    features: ["road", "water", "land", "building"],
                    styleJson:styleJson
                    //style:"dark"
                });
                
                newmap.centerAndZoom(new BMap.Point(init_pos.lon, init_pos.lat),init_zoom);
                
                //newmap.centerAndZoom(new BMap.Point(118.807347, 32.048577),13);
                
             	//单击获取点击的经纬度
  				newmap.addEventListener("click",function(e){
  					//alert(e.point.lng + "," + e.point.lat);
  				});
                
                return newmap;
            }
        </script>
    </body>
</html>
