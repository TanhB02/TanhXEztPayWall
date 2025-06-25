(function() {
    try {
        window.$paywall_user_package = IS_FREE_TRIAL;
        console.log('✅ User package isFreeTrial injected successfully');
        return 'Success';
    } catch (e) {
        console.error('❌ User package injection error:', e);
        return 'Error: ' + e.message;
    }
})(); 