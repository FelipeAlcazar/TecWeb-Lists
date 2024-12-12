import { Component } from '@angular/core';

@Component({
  selector: 'app-geolocalizacion',
  standalone: true,
  imports: [],
  templateUrl: './geolocalizacion.component.html',
  styleUrls: ['./geolocalizacion.component.css']
})
export class GeolocalizacionComponent {
  coordenadas?: GeolocationPosition;
  ciudad?: string;
  tiempo?: string;
  temperatura?: number;
  humedad?: number;
  condiciones?: string;

  ngOnInit() {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition((position) => {
        this.coordenadas = position;
        this.obtenerCiudad();
        this.obtenerTiempo();

        navigator.geolocation.watchPosition((position) => {
          this.coordenadas = position;
        });
      });
    }
  }

  obtenerCiudad() {
    if (this.coordenadas) {
      const lat = this.coordenadas.coords.latitude;
      const lon = this.coordenadas.coords.longitude;
      fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}`)
        .then(response => response.json())
        .then(data => {
          this.ciudad = data.address.city || data.address.town || data.address.village || 'Unknown';
        })
        .catch(error => {
          console.error('Error fetching city:', error);
        });
    }
  }

  obtenerTiempo() {
    if (this.coordenadas) {
      const lat = this.coordenadas.coords.latitude;
      const lon = this.coordenadas.coords.longitude;
      const apiKey = 'YFFLKE5YNPHWABV5U8N3LGA2E'; // Replace with your Visual Crossing API key
      const url = `https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/${lat},${lon}?unitGroup=metric&key=${apiKey}`;
      fetch(url)
        .then(response => response.json())
        .then(data => {
          const currentConditions = data.currentConditions;
          this.tiempo = currentConditions.datetime;
          this.temperatura = currentConditions.temp;
          this.humedad = currentConditions.humidity;
          this.condiciones = currentConditions.conditions;
        })
        .catch(error => {
          console.error('Error fetching weather:', error);
        });
    }
  }
}