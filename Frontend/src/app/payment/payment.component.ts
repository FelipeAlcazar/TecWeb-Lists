import { Component, AfterViewInit, AfterViewChecked, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { loadStripe, Stripe } from '@stripe/stripe-js';
import { PagosService } from '../pagos.service';

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment.component.html',
  styleUrls: ['./payment.component.css']
})
export class PaymentComponent implements AfterViewInit, AfterViewChecked, OnInit {
  stripe: Stripe | null = null;
  cardElement: any;
  @Output() paymentSuccess = new EventEmitter<void>();
  @Output() paymentCancel = new EventEmitter<void>();

  constructor(private pagosService: PagosService) {}

  ngOnInit() {}

  ngAfterViewInit() {
    this.initializeStripe();
  }

  ngAfterViewChecked() {
    if (!this.stripe) {
      this.initializeStripe();
    }
  }

  async initializeStripe() {
    if (!document.getElementById('card-element')) {
      return;
    }
    this.stripe = await loadStripe('pk_test_51Q7a6EP5FVnktAtkRy68Q9Pu55S5S3OmktjrTC5J36U0NKzNxOTmjztKi1Nrirdds39QLAKnyVVRrcDzG3xLn3VS00rAbwGfBt');
    const elements = this.stripe?.elements();
    this.cardElement = elements?.create('card');
    this.cardElement?.mount('#card-element');
  }

  async confirmPayment() {
    if (!this.stripe || !this.cardElement) {
      return;
    }

    const { paymentMethod, error } = await this.stripe.createPaymentMethod({
      type: 'card',
      card: this.cardElement,
    });

    if (error) {
      console.error('Payment error:', error);
      return;
    }

    const token = localStorage.getItem('authToken');
    if (!token) {
      console.error('No auth token found');
      return;
    }

    const importe = 3.00;

    this.pagosService.prepararTransaccion(importe, token, paymentMethod.id).subscribe(
      async response => {
        const clientSecret = response.body?.clientSecret;

        if (clientSecret) {
          try {
            const paymentIntentResult = await this.stripe?.confirmCardPayment(clientSecret, {
              payment_method: paymentMethod.id,
              return_url: 'http://localhost:4200/GestorListas'
            });

            if (paymentIntentResult?.error) {
              if (paymentIntentResult.error.type === 'invalid_request_error' && paymentIntentResult.error.code === 'payment_intent_unexpected_state') {
                if (paymentIntentResult.error.payment_intent?.status === 'succeeded') {
                  console.log('Payment succeeded despite error');
                  this.paymentSuccess.emit();
                } else {
                  console.error('Payment confirmation error:', paymentIntentResult.error);
                }
              } else {
                console.error('Payment confirmation error:', paymentIntentResult.error);
              }
            } else if (paymentIntentResult?.paymentIntent?.status === 'succeeded') {
              this.paymentSuccess.emit();
            }
          } catch (error: any) {
            if (error.type === 'invalid_request_error' && error.code === 'payment_intent_unexpected_state') {
              if (error.payment_intent?.status === 'succeeded') {
                console.log('Payment succeeded despite error');
                this.paymentSuccess.emit();
              } else {
                console.error('Payment confirmation error:', error);
              }
            } else {
              console.error('Payment confirmation error:', error);
            }
          }
        }
      },
      error => {
        console.error('Prepare transaction error:', error);
      }
    );
  }

  cancelPayment() {
    this.paymentCancel.emit();
  }
}