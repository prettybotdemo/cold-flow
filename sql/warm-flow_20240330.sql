update flow_node
set listener_path = REPLACE(listener_path, ',', '@@');