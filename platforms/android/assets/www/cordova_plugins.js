cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
    {
        "file": "plugins/com.ququplay.websocket.WebSocket/www/phonegap-websocket.js",
        "id": "com.ququplay.websocket.WebSocket.websocket",
        "clobbers": [
            "WebSocket"
        ]
    }
];
module.exports.metadata = 
// TOP OF METADATA
{
    "com.ququplay.websocket.WebSocket": "0.1.0"
}
// BOTTOM OF METADATA
});