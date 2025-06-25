(function() {
    try {
        window.$paywall_payload = PAYLOAD_DATA;
        console.log('✅ Payload injected successfully');
        return 'Success';
    } catch (e) {
        console.error('❌ Injection error:', e);
        return 'Error: ' + e.message;
    }
})();

