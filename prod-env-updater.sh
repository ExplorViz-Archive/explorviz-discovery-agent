# Update backend ip
sed -i "s#backendIP=.*#backendIP=$BACKEND_IP#g" META-INF/explorviz-custom.properties

# Update backend port
sed -i "s#backendPort=.*#backendPort=$BACKEND_PORT#g" META-INF/explorviz-custom.properties

# Update agent server ip
sed -i "s#server.ip=.*#server.ip=$SERVER_IP#g" META-INF/explorviz-custom.properties

# Update agent server port
sed -i "s#server.port=.*#server.port=$SERVER_PORT#g" META-INF/explorviz-custom.properties