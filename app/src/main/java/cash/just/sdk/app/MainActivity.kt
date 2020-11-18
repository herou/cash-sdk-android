package cash.just.sdk.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import cash.just.sdk.Cash
import cash.just.sdk.CashSDK
import cash.just.sdk.model.*
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Response
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        afterLoginPanel.visibility = View.GONE

        guestLoginButton.setOnClickListener {
            CashSDK.createGuestSession(getServer(), object: Cash.SessionCallback {
                override fun onSessionCreated(sessionKey: String) {
                    session.setText(sessionKey)
                    afterLoginPanel.visibility = View.VISIBLE
                }

                override fun onError(errorMessage: String?) {
                    Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
                }
            })
        }

        loginButton.setOnClickListener {
            CashSDK.login(getServer(), userPhoneNumber.text.toString(), object: Cash.WacCallback {
                override fun onSucceed() {
                    Toast.makeText(applicationContext, "on succeed", Toast.LENGTH_SHORT).show()
                }

                override fun onError(errorMessage: String?) {
                    Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
                }
            })
        }

        kycButton.setOnClickListener {
            CashSDK.getKycStatus().enqueue(object: retrofit2.Callback<KycStatusResponse> {
                override fun onResponse(call: Call<KycStatusResponse>, response: Response<KycStatusResponse>) {
                    if (response.code() == 200) {
                        Toast.makeText(applicationContext, response.toString(), Toast.LENGTH_LONG).show()
                    } else {
                        response.errorBody()?.let {
                            val error = it.parseError()
                            Toast.makeText(applicationContext, "Request with error: ${error.error.code}", Toast.LENGTH_LONG).show()
                        } ?:run {
                            Timber.e("http code is not 200 and it has no errorBody")
                        }
                    }
                }

                override fun onFailure(call: Call<KycStatusResponse>, t: Throwable) {

                }
            })
        }

        registerButton.setOnClickListener {
            val server = getServer()
            CashSDK.register(server,
                registerPhoneNumber.text.toString(),
                registerName.text.toString(),
                registerSurname.text.toString(), object:Cash.WacCallback {
                    override fun onSucceed() {
                        Toast.makeText(applicationContext, "registered", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(errorMessage: String?) {
                        Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
                    }
            })
        }

        getAtmList.setOnClickListener {
            list.text.clear()
            CashSDK.getAtmList().enqueue(object: retrofit2.Callback<AtmListResponse> {
                override fun onFailure(call: Call<AtmListResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<AtmListResponse>, response: Response<AtmListResponse>) {
                    list.setText(response.body()!!.data.toString())
                }
            })
        }

        getAtmListByLatitude.setOnClickListener {
            list.text.clear()
            CashSDK.getAtmListByLocation(lat.text.toString(), lon.text.toString())
                .enqueue(object: retrofit2.Callback<AtmListResponse> {
                    override fun onFailure(call: Call<AtmListResponse>, t: Throwable) {
                        Toast.makeText(applicationContext, t.message, Toast.LENGTH_SHORT).show()
                    }

                    override fun onResponse(call: Call<AtmListResponse>, response: Response<AtmListResponse>) {
                        list.setText(response.body()!!.data.toString())
                    }
            })
        }

        checkCode.setOnClickListener {
            CashSDK.checkCashCodeStatus(code.text.toString()).enqueue(object: retrofit2.Callback<CashCodeStatusResponse> {
                override fun onResponse(call: Call<CashCodeStatusResponse>, response: Response<CashCodeStatusResponse>) {
                    if (response.isSuccessful
                        && response.body() != null
                        && response.body()!!.data != null
                        && response.body()!!.data!!.items.isNotEmpty()
                    ) {
                        val result = response.body()!!.data!!.items[0]
                        Toast.makeText(applicationContext, result.getCodeStatus().toString() + " " + result.toString(), Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(applicationContext, response.code().toString(), Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<CashCodeStatusResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_SHORT).show()
                }
            })
        }

        sendVerificationCode.setOnClickListener {
            CashSDK.sendVerificationCode(
                firstName.text.toString(),
                lastName.text.toString(),
                phoneNumber.text.toString(),
                email.text.toString()).enqueue(object: retrofit2.Callback<SendVerificationCodeResponse> {
                override fun onFailure(call: Call<SendVerificationCodeResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<SendVerificationCodeResponse>, responseVerification: Response<SendVerificationCodeResponse>) {
                    if (responseVerification.isSuccessful) {
                        Toast.makeText(applicationContext, responseVerification.body()!!.data.toString(), Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(applicationContext, responseVerification.code().toString(), Toast.LENGTH_LONG).show()
                    }
                }
            })
        }

        createCode.setOnClickListener {
            CashSDK.createCashCode(
                atmId.text.toString(),
                amount.text.toString(),
                verificationCode.text.toString()).enqueue(object: retrofit2.Callback<CashCodeResponse> {
                override fun onFailure(call: Call<CashCodeResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<CashCodeResponse>, response: Response<CashCodeResponse>) {
                    if (response.isSuccessful) {
                        val responseText = response.body()!!.data.toString()
                        createCodeResult.setText(responseText)
                        Toast.makeText(applicationContext, responseText, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(applicationContext, response.code().toString(), Toast.LENGTH_LONG).show()
                    }
                }
            })
        }
    }

    private fun getServer(): Cash.BtcNetwork {
        return if (serverToggleButton.isChecked) Cash.BtcNetwork.MAIN_NET
        else Cash.BtcNetwork.TEST_NET
    }
}
