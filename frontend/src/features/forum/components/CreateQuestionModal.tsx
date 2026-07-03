import { useState, type FormEvent } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getCarreras } from '@/features/auth/services/authService';
import { getAsignaturas } from '@/features/groups/services/groupsService';
import type { CreatePreguntaPayload } from '@/features/forum/types/forum.types';
import { Button, Input, Select } from '@/shared/components/ui/FormControls';

interface CreateQuestionModalProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (payload: CreatePreguntaPayload) => Promise<void>;
}

export function CreateQuestionModal({ open, onClose, onSubmit }: CreateQuestionModalProps) {
  const [titulo, setTitulo] = useState('');
  const [contenido, setContenido] = useState('');
  const [carreraId, setCarreraId] = useState('');
  const [asignaturaId, setAsignaturaId] = useState('');
  const [loading, setLoading] = useState(false);

  const carrerasQuery = useQuery({ queryKey: ['carreras'], queryFn: getCarreras });
  const asignaturasQuery = useQuery({
    queryKey: ['asignaturas', carreraId],
    queryFn: () => getAsignaturas(carreraId),
    enabled: !!carreraId,
  });

  if (!open) return null;

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setLoading(true);
    try {
      await onSubmit({ titulo, contenido, carreraId, asignaturaId });
      setTitulo('');
      setContenido('');
      setCarreraId('');
      setAsignaturaId('');
      onClose();
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-2xl bg-white p-6 shadow-xl">
        <div className="mb-6 flex items-center justify-between">
          <h2 className="text-xl font-bold">Nueva pregunta</h2>
          <button type="button" onClick={onClose} className="text-gray-400 hover:text-gray-600">
            ✕
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            label="Título"
            name="titulo"
            value={titulo}
            onChange={(e) => setTitulo(e.target.value)}
            placeholder="Resume tu duda en una frase"
            required
          />

          <div className="space-y-1">
            <label htmlFor="contenido" className="block text-sm font-medium text-gray-700">
              Detalle
            </label>
            <textarea
              id="contenido"
              value={contenido}
              onChange={(e) => setContenido(e.target.value)}
              rows={5}
              required
              className="w-full rounded-lg border border-gray-300 px-3 py-2.5 text-sm outline-none focus:border-duoc-yellow focus:ring-2 focus:ring-yellow-100"
              placeholder="Describe tu pregunta con el mayor detalle posible..."
            />
          </div>

          <Select
            label="Carrera"
            name="carreraId"
            value={carreraId}
            onChange={(e) => {
              setCarreraId(e.target.value);
              setAsignaturaId('');
            }}
            options={(carrerasQuery.data ?? []).map((c) => ({ value: c.id, label: c.nombre }))}
            required
          />

          <Select
            label="Asignatura"
            name="asignaturaId"
            value={asignaturaId}
            onChange={(e) => setAsignaturaId(e.target.value)}
            options={(asignaturasQuery.data ?? []).map((a) => ({ value: a.id, label: a.nombre }))}
            required
          />

          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={onClose}>
              Cancelar
            </Button>
            <Button type="submit" loading={loading}>
              Publicar pregunta
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
