package com.house.security;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
/**
 * A login screen that offers login via email/password.
 */
@SuppressLint("NewApi")
public class Admin extends Activity implements LoaderCallbacks<Cursor> {

	/**
	 * A dummy authentication store containing known user names and passwords.
	 * TODO: remove after connecting to a real authentication system.
	 */
	private static final String[] DUMMY_CREDENTIALS = new String[] {
			"admin@subhahu.com:admin123", "bar@example.com:world" };
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// UI references.
	private AutoCompleteTextView mEmailView;
	private EditText mPasswordView;
	private View mProgressView;
	private View mLoginFormView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_admin);
		setupActionBar();

		// Set up the login form.
		mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
		populateAutoComplete();

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
		mEmailSignInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptLogin();
			}
		});

		mLoginFormView = findViewById(R.id.login_form);
		mProgressView = findViewById(R.id.login_progress);

	if (util.getPreference(Admin.this,"LoginBlocked",false))
	{
		mEmailView.setText("account expired");
	}

		ActionBar actionBar = getActionBar();
		actionBar.setTitle("Login");
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#42A5F5")));
		actionBar.setDisplayHomeAsUpEnabled(false);
	}

	@SuppressLint("NewApi")
	private void populateAutoComplete() {
		if (VERSION.SDK_INT >= 14) {
			// Use ContactsContract.Profile (API 14+)
			getLoaderManager().initLoader(0, null, this);
		} else if (VERSION.SDK_INT >= 8) {
			// Use AccountManager (API 8+)
			new SetupEmailAutoCompleteTask().execute(null, null);
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#42A5F5")));
		}
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		String email = mEmailView.getText().toString();
		String password = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password, if the user entered one.
		if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(email)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!isEmailValid(email)) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			showProgress(true);
			mAuthTask = new UserLoginTask(getApplicationContext(), email, password);
			mAuthTask.execute((Void) null);
		}
	}

	private boolean isEmailValid(String email) {
		// TODO: Replace this with your own logic
		return email.contains("@");
	}

	private boolean isPasswordValid(String password) {
		// TODO: Replace this with your own logic
		return password.length() >= 4;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:
				this.finish();
				return true;
		}
		return true;
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});

			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mProgressView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mProgressView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	@SuppressLint("NewApi")
	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		return new CursorLoader(this,
				// Retrieve data rows for the device user's 'profile' contact.
				Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
						ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
				ProfileQuery.PROJECTION,

				// Select only email addresses.
				ContactsContract.Contacts.Data.MIMETYPE + " = ?",
				new String[] { ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE },

				// Show primary email addresses first. Note that there won't be
				// a primary email address if the user hasn't specified one.
				ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		List<String> emails = new ArrayList<String>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			emails.add(cursor.getString(ProfileQuery.ADDRESS));
			cursor.moveToNext();
		}

		addEmailsToAutoComplete(emails);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader) {

	}

	private interface ProfileQuery {
		String[] PROJECTION = { ContactsContract.CommonDataKinds.Email.ADDRESS,
				ContactsContract.CommonDataKinds.Email.IS_PRIMARY, };

		int ADDRESS = 0;
		int IS_PRIMARY = 1;
	}

	/**
	 * Use an AsyncTask to fetch the user's email addresses on a background
	 * thread, and update the email text field with results on the main UI
	 * thread.
	 */
	class SetupEmailAutoCompleteTask extends
			AsyncTask<Void, Void, List<String>> {

		@Override
		protected List<String> doInBackground(Void... voids) {
			ArrayList<String> emailAddressCollection = new ArrayList<String>();

			// Get all emails from the user's contacts and copy them to a list.
			ContentResolver cr = getContentResolver();
			Cursor emailCur = cr.query(
					ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
					null, null, null);
			while (emailCur.moveToNext()) {
				String email = emailCur
						.getString(emailCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
				emailAddressCollection.add(email);
			}
			emailCur.close();

			return emailAddressCollection;
		}

		@Override
		protected void onPostExecute(List<String> emailAddressCollection) {
			addEmailsToAutoComplete(emailAddressCollection);
		}
	}

	

	private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
		// Create adapter to tell the AutoCompleteTextView what to show in its
		// dropdown list.
		/*
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				LoginActivity.this,
				android.R.layout.simple_dropdown_item_1line,
				emailAddressCollection);

		mEmailView.setAdapter(adapter);
		*/
	}

	public static String entityToString(HttpEntity entity) throws IllegalStateException, IOException {
	  InputStream is = entity.getContent();
	  BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
	  StringBuilder str = new StringBuilder();
	
	  String line = null;
	  try {
		while ((line = bufferedReader.readLine()) != null) {
		  str.append(line + "\n");
		}
	  } catch (IOException e) {
		throw new RuntimeException(e);
	  } finally {
		try {
		  is.close();
		} catch (IOException e) {
		  //tough luck...
		}
	  }
	  return str.toString();
	}


	private boolean makeLogin (String username, String password) throws JSONException
	{
	
		Log.d("QR_SCAN_SERVICE","makeLogin "+username +" "+password); 

		HttpClient httpClient = new DefaultHttpClient();
	  	HttpPost httpPost = new HttpPost("http://ck-monitor.herokuapp.com/api/users/auth");

		httpPost.addHeader("Content-Type", "application/json");
		String json = "";

		//JSONArray jsonObject = new JSONArray();
		JSONObject jsonObject = new JSONObject();

		//JSONObject jsonObject_location = new JSONObject();  
		try {
			jsonObject.accumulate("email", username);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			jsonObject.accumulate("password", password);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// 4. convert JSONObject to JSON to String
		json = jsonObject.toString();
		Log.d("QR_SCAN_SERVICE","json string "+json);
		
		StringEntity se = null;
		try {
			se = new StringEntity(json);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Encoding data
		httpPost.setEntity(se);

		String auth_key = null;
		boolean result = true;

		// making request
		try {
			HttpResponse response = httpClient.execute(httpPost);
			// write response to log				
			String resp = entityToString(response.getEntity());
			Log.d("QR_SCAN_SERVICE","makeLogin response entity "+resp);

			JSONObject jsonObj = null;
			try {
				jsonObj = new JSONObject(resp);
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				Log.d("QR_SCAN_SERVICE","Auto-generated catch block 1");
				result = false;
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.d("QR_SCAN_SERVICE","Auto-generated catch block 2");
				result = false;
				e.printStackTrace();
			}

			if (result == false)
			{
				Log.d("QR_SCAN_SERVICE","Auto-generated catch block return false");
			 	return false;
			}
			auth_key = jsonObj.getString("authorizeKey");

			if(null != auth_key)
			{
				Log.d("QR_SCAN_SERVICE","makeLogin auth_key "+auth_key);
				util.savePreference(Admin.this,"authorizeKey",auth_key);
				util.savePreference(Admin.this,"client_name",username);
				util.savePreference(Admin.this,"client_password",password);
			}

 
		
			JSONObject jsonobj_user = jsonObj.getJSONObject("user");
			
			if (jsonobj_user == null)
			{				
				return (null == auth_key)?false:true;
			}
			
			String _role = jsonobj_user.getString("role");
			Log.d("QR_SCAN_SERVICE","makeLogin user length() "+jsonobj_user.length() +" _role "+_role);

			if (0 != jsonobj_user.length())
			{
				JSONArray jsonArray_user = jsonobj_user.getJSONArray("companies");
				
				if (jsonArray_user == null || 0 == jsonArray_user.length())
				{	
					if (jsonArray_user != null)
					{
						Log.d("QR_SCAN_SERVICE","makeLogin comapanies Null case array length() "+jsonobj_user.length());
						String company = jsonobj_user.optString("company");
						Log.d("QR_SCAN_SERVICE","makeLogin company "+company);
						if (0 == jsonArray_user.length() && auth_key != null)
						{
							util.addToHashSet(Admin.this, "authKey");

						}
						if(!company.isEmpty()) {
							util.savePreference(Admin.this,"companyId",company);
						}

					}
										
					return (null == auth_key)?false:true;
				}		
				
				Log.d("QR_SCAN_SERVICE","makeLogin comapanies array length() "+jsonArray_user.length());
				for (int j = 0; j < jsonArray_user.length(); j++)
				{
					JSONObject user_jsonObject = jsonArray_user.getJSONObject(j);

					//String _role = user_jsonObject.getString("role");

					//if(_role.equals("comapany_admin"))
					{
						if (0 != jsonArray_user.length())
						{
							Log.d("QR_SCAN_SERVICE","makeLogin response companies arrayLen "+jsonArray_user.length());
							int arrLen = jsonArray_user.length();
							//for (int i = 0; i < jsonArray_user.length(); i++)
							{
								JSONObject _jsonObject = jsonArray_user.getJSONObject(j);
								String _id = _jsonObject.optString("_id").toString();
								String displayName = _jsonObject.optString("displayName").toString();
								Log.d("QR_SCAN_SERVICE","displayName "+displayName +" _id "+_id);
								util.addToHashSet(Admin.this, _id);
							}
						}
					}
				}
			}
		
		} catch (ClientProtocolException e) {
			// Log exception
			e.printStackTrace();
		} catch (IOException e) {
			// Log exception
			e.printStackTrace();
		}

		return (null == auth_key)?false:true;

	}


	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

		private final String mEmail;
		private final String mPassword;
		private Context mContext;

		UserLoginTask(Context context, String email, String password) {
			mEmail = email;
			mPassword = password;
			mContext = context;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.
			Log.d("QR_SCAN_ADMIN","doInBackground ");

			if (util.getPreference(Admin.this,"LoginBlocked",false))
			{
				Log.d("QR_SCAN_ADMIN"," account expired");
				//return false;
			}
			
			try {
				// Simulate network access.
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				return false;
			}

			for (String credential : DUMMY_CREDENTIALS) {
				String[] pieces = credential.split(":");
				Log.d("QR_SCAN_ADMIN","login "+mEmail +" "+mPassword);
				Log.d("QR_SCAN_ADMIN","login "+pieces[0] +" "+pieces[1]);
				
				if (pieces[0].equals(mEmail)) {
					// Account exists, return true if the password matches.
					if (pieces[1].equals(mPassword))
					{
						util.savePreference(Admin.this,"login_clent_type","no");
						Log.d("QR_SCAN_ADMIN","admin login ");
					}
					return pieces[1].equals(mPassword);
				}
			}

			String login_name = util.getPreference(Admin.this, "client_name", null);
			String login_password = util.getPreference(Admin.this, "client_password", null);
			String authorizeKey = util.getPreference(Admin.this, "authorizeKey", null);

			Log.d("QR_SCAN_ADMIN","login_password " +login_password+" "+login_name +" "+authorizeKey);

			if (null != login_name && null != login_password && null != authorizeKey)
			{
				if ((login_name+"@subhahu.com").equals(mEmail)) {
					// Account exists, return true if the password matches.
					if (login_password.equals(mPassword))
					{
						util.savePreference((Context)Admin.this,"login_clent_type","yes");
						Log.d("QR_SCAN_ADMIN","client login ");
					}
									
					Intent intent_br = new Intent();
					intent_br.setAction("com.sec.loin.done");
					sendBroadcast(intent_br);
					
					return login_password.equals(mPassword);
				}
			}

			boolean result = false;
			try {
					result = makeLogin (mEmail,mPassword);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			if (result == true)
			{
//				 Intent intent_br = new Intent();
//				 intent_br.setAction("com.sec.loin.done");
//				sendBroadcast(intent_br);

				Log.d("QR_SCAN_ADMIN","login success sendBroadcast ");
			}
			return result;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
				Log.d("QR_SCAN_ADMIN","login success ");
				//finish();

				util.savePreference(Admin.this,"isFirstTime",true);

				try {

					Calendar c = Calendar.getInstance();
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
					String loginDate = formatter.format(c.getTime());
					util.savePreference(Admin.this,"loginDate",loginDate);
					Log.d("QR_SCAN_ADMIN","login date "+loginDate);

					Intent intent_br = new Intent();
					intent_br.setAction("com.sec.loin.done");
					sendBroadcast(intent_br);

				
					//Intent intent = new Intent(ACTION_SCAN);
					Intent intent = new Intent();		
					intent.setClass(getApplicationContext(), HistoryMainPage.class);
					intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
					startActivityForResult(intent, 0);

					finish ();
				} catch (ActivityNotFoundException anfe) {
					
				}
			} else {
				mPasswordView
						.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
}
