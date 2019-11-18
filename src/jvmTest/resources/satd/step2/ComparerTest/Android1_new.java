package satd.step2;

class Class1 {
    /**
     * Overrides IntentReceiver.onReceiveIntent() to show the necessary UI notification for handling SMS results (success or errors)
     * and stopping the progress bar once that all SMSs have been sent
     */
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        SendSMSActivity activity = (SendSMSActivity) context;
        sendBtn = (ImageButton) activity.findViewById(R.id.sendsmsBtn);
        progDlg = activity.getProgressDialog();
        ctx = context;
        if (action.equals(SendSMSActivity.SMS_SENT_ACTION)) {
            messageBuf = new StringBuffer();
            messageBuf.append("Message was delivered successfully");
            recvHandler.postDelayed(new Runnable() {

                public void run() {
                    progDlg.dismiss();
                    Toast.makeText(ctx, messageBuf.toString(), 2000).show();
                    sendBtn.setEnabled(true);
                }
            }, 3000);
        }
    }
}
